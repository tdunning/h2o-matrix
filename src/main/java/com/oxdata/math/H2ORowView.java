package com.oxdata.math;

import java.util.Iterator;

import org.apache.mahout.math.*;

import water.fvec.Chunk;
import water.H2O;

/**
 * Provides a virtual vector that is really an H2O row
 */
public class H2ORowView extends AbstractVector {
  private final Chunk _chks[];
  public int _row;              // Public access, public modification

  public H2ORowView(Chunk chks[]) { super(chks.length); _chks = chks; }
  public H2ORowView ofRow( int row ) { _row=row; return this; }
  @Override public boolean isDense() { return true; }
  @Override public boolean isSequentialAccess() { return false; }
  @Override public Iterator<Element> iterator() {
    final LocalElement r = new LocalElement(0);
    return new Iterator<Element>() {
      private int i;
      @Override public boolean hasNext() { return i < _chks.length; }
      @Override public Element next() { r.i = i++; return r; }
      @Override public void remove() { throw new UnsupportedOperationException("Can't remove from a view"); }
    };
  }
  protected final class LocalElement implements Element {
    int i;
    LocalElement(int index) { i = index; }
    @Override public double get() { return getQuick(i); }
    @Override public int index() { return i; }
    @Override public void set(double value) { setQuick(i, value); }
  }
  @Override public Iterator<Element> iterateNonZero() { return iterator(); }
  @Override public double getQuick(int index) { return _chks[index].at0(_row); }
  @Override public Vector like() { throw H2O.unimpl(); }
  @Override public void setQuick(int index, double value) { _chks[index].set0(_row,value); }
  @Override public int getNumNondefaultElements() { return size(); }
  @Override public double getLookupCost() { return 1; }
  @Override public double getIteratorAdvanceCost() { return 1; }
  @Override public boolean isAddConstantTime() { return true; }
  @Override protected Matrix matrixLike(int rows, int columns) { throw H2O.unimpl(); }
  @Override public void mergeUpdates(OrderedIntDoubleMapping updates) { throw H2O.unimpl(); }
}
