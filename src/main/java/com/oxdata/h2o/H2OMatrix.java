package com.oxdata.h2o;

import org.apache.mahout.math.AbstractMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.MatrixSlice;
import org.apache.mahout.math.Vector;

import water.Futures;
import water.Key;
import water.H2O;
import water.fvec.*;

/**
 * Implement a simple matrix type to emulate what an h2o based matrix would need.
 */
public class H2OMatrix extends AbstractMatrix {
  public/*private*/ Frame _fr;

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

  @Override public double getQuick(int row, int column) { return _fr.vecs()[column].at(row); }
  @Override public Matrix like() { return new H2OMatrix(_fr.deepSlice(null,null)); }
  @Override public Matrix like(int rows, int columns) { return new H2OMatrix(this.rows, this.columns); }
  @Override public void setQuick(int row, int column, double value) { _fr.vecs()[column].set(row,value); }
}
