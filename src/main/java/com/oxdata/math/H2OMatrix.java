package com.oxdata.math;

import org.apache.mahout.math.*;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.VectorFunction;

import water.*;
import water.fvec.*;

/**
 * Implement a simple matrix type to emulate what an h2o based matrix would need.
 */
public class H2OMatrix extends AbstractMatrix implements Freezable {
  private Frame _fr;

  public void delete() { _fr.delete(); }

  public H2OMatrix(int rows, int columns) {
    super(rows, columns);
    Vec[] vecs = new Vec[columns];
    for( int i=0; i<columns; i++ )
      vecs[i] = Vec.makeConSeq(0,rows);
    _fr = new Frame(vecs);
  }

  public H2OMatrix(Matrix original) {
    super(original.rowSize(), original.columnSize());
    if( original instanceof H2OMatrix ) {
      _fr = ((H2OMatrix)original)._fr.deepSlice(null,null);
    } else{
      int columns = original.columnSize();
      Key keys[] = Vec.VectorGroup.VG_LEN1.addVecs(columns);
      AppendableVec avs[] = new AppendableVec[columns];
      Vec[] vecs = new Vec[columns];
      NewChunk[] ncs = new NewChunk[columns];
      for( int col=0; col<columns; col++ )
        ncs[col] = new NewChunk(avs[col] = new AppendableVec(keys[col]),0);
      // "hard" iteration direction: fill all columns in parallel, row-by-row
      for( MatrixSlice row : original ) {
        int col=0;
        for( Vector.Element element : row.all() )
          ncs[col++].addNum(element.get());
      }
      Futures fs = new Futures();
      for( int col=0; col<columns; col++ ) {
        ncs[col].close(0,fs);
        vecs[col] = avs[col].close(fs);
      }
      fs.blockForPending();
      _fr = new Frame(vecs);
    }
  }
  private H2OMatrix( Frame fr ) {
    super((int) fr.numRows(), fr.numCols());
    _fr = fr;
    if( _fr.numRows() > Integer.MAX_VALUE )
      throw new IllegalArgumentException("AbstractMatrix does not support more than 2^31 rows");
  }

  @Override public Matrix assignColumn(int column, Vector other) {
    if( other.size() != _fr.numRows() ) 
      throw new IllegalArgumentException("other has "+other.size()+" rows, but this matrix has "+_fr.numRows()+" rows");
    H2OVector hvec = other instanceof H2OVector ? (H2OVector)other : new H2OVector(other);
    _fr.replace(column,hvec._vec);
    return this;
  }

  // Calling this likely indicates a huge performance bug.
  @Override public Matrix assignRow(int row, Vector other) {
    if( other.size() != _fr.numCols() ) 
      throw new IllegalArgumentException("other has "+other.size()+" columns, but this matrix has "+_fr.numCols()+" columns");
    Futures fs = new Futures();
    Vec vecs[] = _fr.vecs();
    for( int i=0; i<vecs.length; i++ ) {
      vecs[i].set(row,other.getQuick(i));
      vecs[i].chunkForRow(row).close(0,fs);
      vecs[i].postWrite();
    }
    fs.blockForPending();
    return this;
  }

  // Single-element accessors.  Calling these likely indicates a huge performance bug.
  @Override public double getQuick(int row, int column) { return _fr.vecs()[column].at(row); }
  @Override public void setQuick(int row, int column, double value) { _fr.vecs()[column].set(row,value); _fr.vecs()[column].chunkForRow(row).close(0,null); }
  // Clone an entire array
  @Override public Matrix like() { return new H2OMatrix(_fr.deepSlice(null,null)); }
  @Override public Matrix like(int rows, int columns) { return new H2OMatrix(this.rows, this.columns); }
  // Shared column slice.
  @Override public Vector viewColumn(int column) { return new H2OVector( _fr.vecs()[column] ); }

  @Override public double aggregate(DoubleDoubleFunction aggregator, DoubleFunction map) {
    double res = viewColumn(0).aggregate(aggregator,map);
    for( int i=0; i<_fr.numCols(); i++ )
      res = aggregator.apply(res,viewColumn(i).aggregate(aggregator,map));
    return res;
  }

  // Break the data into rows, supply each row to a row-wise aggegrator function
  @Override public Vector aggregateRows(final VectorFunction f) {
    if( !(f instanceof H2OVectorFunction) )
      throw new IllegalArgumentException("H2OMatrix ops only run well with H2O Functions; found "+f.getClass());
    final H2OVectorFunction f2 = (H2OVectorFunction)f;
    return new H2OVector(new MRTask2() {
       @Override public void map( Chunk chks[], NewChunk nc ) {
         int n = chks[0]._len;
         H2ORowView h2orv = new H2ORowView(chks);
         for (int row = 0; row < n; row++)
           nc.addNum(f2.apply(h2orv.ofRow(row)));
       }
     }.doAll(1,_fr).outputFrame(null,null).vecs()[0]);
  }

  // apply function f to each element in-place
  @Override public Matrix assign( DoubleFunction f ) {
    final H2ODoubleFunction f2 = H2ODoubleFunction.map(f);
    new MRTask2() {
      @Override protected void setupLocal() { f2.setupLocal(); }
      @Override public void map( Chunk chks[] ) {
        for( Chunk c : chks )
          for( int row=0; row<c._len; row++ )
            c.set0(row,f2.apply(c.at0(row)));
      }
    }.doAll(_fr);
    return this;
  }

  // apply function f(x) to each element in-place.  Similar to "this op= x";
  @Override public Matrix assign( Matrix x, DoubleDoubleFunction f ) {
    if( !(x instanceof H2OMatrix) )
      throw new IllegalArgumentException("Mixing H2OMatrix with "+x.getClass());
    final H2OMatrix x2 = (H2OMatrix)x;
    if( _fr.numRows() != x2._fr.numRows() ) throw new CardinalityException((int)_fr.numRows(),(int)x2._fr.numRows());
    if( _fr.numCols() != x2._fr.numCols() ) throw new CardinalityException(     _fr.numCols(),     x2._fr.numCols());
    final H2ODoubleDoubleFunction f2 = H2ODoubleDoubleFunction.map(f);
    new MRTask2() {
      @Override public void map( Chunk chks[] ) {
        int numc = chks.length>>1;
        for( int col = 0; col<numc; col++ ) {
          Chunk chkl = chks[col     ];
          Chunk chkr = chks[col+numc];
          for( int row=0; row<chkl._len; row++ )
            chkl.set0(row,f2.apply(chkl.at0(row),chkr.at0(row)));
        }
      }
    }.doAll( new Frame(_fr).add(x2._fr,true));
    return this;
  }

  // Returns this-x
  @Override public Matrix minus( Matrix x ) { return like().assign(x,H2ODoubleDoubleFunction.MINUS); }


  // ---
  // Normally auto-gened by H2O's Weaver, but must inherit from AbstractMatrix
  // instead of either Iced or DTask.
  @Override public AutoBuffer write(AutoBuffer bb) { return bb.put(_fr); }
  @Override public H2OMatrix read(AutoBuffer bb) { 
    _fr = bb.get(); 
    assert rows==-1 && columns==-1; // Set to -1 from private constructor
    rows = (int) _fr.numRows();     // Now set AbstractMatrix fields from frame
    columns = _fr.numCols(); 
    return this; 
  }
  public void copyOver( Freezable that ) { _fr = ((H2OMatrix)that)._fr; }
  public H2OMatrix() { super(-1,-1); }
  @Override public H2OMatrix newInstance() { return new H2OMatrix(); }
  private static int _frozen$type;
  @Override public int frozenType() {
    return _frozen$type == 0 ? (_frozen$type=water.TypeMap.onIce(H2OMatrix.class.getName())) : _frozen$type;
  }
  @Override public AutoBuffer writeJSONFields(AutoBuffer bb) { return bb; }
  @Override public water.api.DocGen.FieldDoc[] toDocField() { return null; }
  // ---
}
