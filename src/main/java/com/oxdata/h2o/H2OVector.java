package com.oxdata.h2o;

import org.apache.mahout.math.AbstractVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.OrderedIntDoubleMapping;
import org.apache.mahout.math.Vector;
import java.util.Iterator;

import water.Futures;
import water.H2O;
import water.fvec.*;
import water.fvec.Vec.VectorGroup;

/**
 * Shows how to implement a vector type.
 */
public class H2OVector extends AbstractVector {
  private Vec _vec;

  private H2OVector( Vec vec ) { 
    super((int)vec.length()); 
    if( (int)vec.length() != vec.length() )
      throw new IllegalArgumentException("AbstractVector is limited to 2^31 in length");
    _vec = vec; 
  }
  public H2OVector(int size) {
    super(size);
    if( size > 100000 ) throw H2O.unimpl(); // Should split into chunks
    _vec = Vec.makeConSeq(0,size);
  }

  public H2OVector(Vector r) {
    super(r.size());
    if( r instanceof H2OVector ) {
      _vec = new Frame(((H2OVector)r)._vec).deepSlice(null,null).vecs()[0];
    } else {
      AppendableVec av = new AppendableVec(VectorGroup.VG_LEN1.addVec());
      NewChunk nc = new NewChunk(av,0);
      for( Element element : r.all() )
        nc.addNum(element.get());
      Futures fs = new Futures();
      nc.close(0,fs);
      _vec = av.close(fs);
      fs.blockForPending();
    }
  }

  @Override protected Iterator<Element> iterator() {
    throw H2O.unimpl();
    //// this is a kind of slow iterator
    //return new AbstractIterator<Element>() {
    //  int i = 0;
    //  @Override protected Element computeNext() {
    //    if (i >= size()) return endOfData();
    //    return new Element() {
    //      int index = i++;
    //      @Override public double get() { return H2OVector.this.get(index); }
    //      @Override public int  index() { return index; }
    //      @Override public void set(double value) { H2OVector.this.values[index] = value; }
    //    };
    //  }
    //
    //};
  }

  @Override protected Iterator<Element> iterateNonZero() { return iterator(); }
  @Override protected Matrix matrixLike(int rows, int columns) { return new H2OMatrix(rows, columns); }
  @Override public boolean isDense() { return true; }
  @Override public boolean isSequentialAccess() { return true; }
  @Override public void mergeUpdates(OrderedIntDoubleMapping updates) {
    // this would only get called if !isAddConstantTime
    throw new UnsupportedOperationException("Default operation");
  }
  @Override public double getQuick(int index) { return _vec.at(index); }
  @Override public Vector like() { return new H2OVector(_vec.makeZero()); }
  @Override public void setQuick(int index, double value) { _vec.set(index,value); }
  @Override public int getNumNondefaultElements() { return size(); }
  @Override public double getLookupCost() { return 2/*bigger than local cost*/; }
  @Override public double getIteratorAdvanceCost() { return 1; }
  @Override public boolean isAddConstantTime() { return true; }
}
