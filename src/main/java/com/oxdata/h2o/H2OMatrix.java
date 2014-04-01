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
      Futures fs = new Futures();
      int col=0;
      for( MatrixSlice row : original ) {
        avs[col] = new AppendableVec(keys[col]);
        NewChunk nc = new NewChunk(avs[col],0);
        for( Vector.Element element : row.all() )
          nc.addNum(element.get());
        nc.close(0,fs);
        vecs[col] = avs[col].close(fs);
        col++;
      }
      fs.blockForPending();
    }
  }
  private H2OMatrix( Frame fr ) {
    super((int) fr.numRows(), fr.numCols());
    _fr = fr;
    if( _fr.numRows() > Integer.MAX_VALUE )
      throw new IllegalArgumentException("AbstractMatrix does not support more than 2^31 rows");
  }

  @Override public Matrix assignColumn(int column, Vector other) {
    throw H2O.unimpl();
  }

  @Override
  public Matrix assignRow(int row, Vector other) {
    throw H2O.unimpl();
  }

  @Override public double getQuick(int row, int column) { return _fr.vecs()[column].at(row); }
  @Override public Matrix like() { return new H2OMatrix(_fr.deepSlice(null,null)); }
  @Override public Matrix like(int rows, int columns) { return new H2OMatrix(this.rows, this.columns); }
  @Override public void setQuick(int row, int column, double value) { _fr.vecs()[column].set(row,value); }
}
