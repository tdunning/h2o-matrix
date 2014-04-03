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
  Vec _vec;

  // Call to clean up H2O storage
  public void delete() { Vec vec = _vec; _vec = null; water.UKV.remove(vec._key); }

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
    // this is a kind of slow iterator.
    // CNC - Speed Hack: return the same Element each time 'round
    return new Iterator<Element>() {
      private Chunk _c = _vec.chunkForRow(0);
      private int _i=-1, _len=(int)_vec.length();
      private Element _elem = new Element() {
          private Chunk iter() { return _c._start <= _i && _i < _c._start+_c._len ? _c : (_c = _vec.chunkForRow(_i)); }
          @Override public double get() { return iter().at(_i-_c._start); }
          @Override public int  index() { return _i; }
          @Override public void set(double value) { iter().set(_i-_c._start,value); }
        };
      @Override public boolean hasNext() { return _i+1<_len; }
      @Override public Element next() { _i++; return _elem; }
      @Override public void remove() { throw H2O.fail(); }
    };
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
