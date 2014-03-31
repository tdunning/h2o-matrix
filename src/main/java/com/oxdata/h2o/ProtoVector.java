package com.oxdata.h2o;

import com.google.common.collect.AbstractIterator;
import org.apache.mahout.math.AbstractVector;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.OrderedIntDoubleMapping;
import org.apache.mahout.math.Vector;

import java.util.Iterator;

/**
 * Shows how to implement a vector type.
 */
public class ProtoVector extends AbstractVector {
  private double[] values;

  public ProtoVector(int size) {
    super(size);
    values = new double[size];
  }

  public ProtoVector(Vector r) {
    super(r.size());
    values = new double[r.size()];
    for( Element element : r.all() )
      values[element.index()] = element.get();
  }

  @Override protected Iterator<Element> iterator() {
    // this is a kind of slow iterator
    return new AbstractIterator<Element>() {
      int i = 0;
      @Override protected Element computeNext() {
        if (i >= size()) return endOfData();
        return new Element() {
          int index = i++;
          @Override public double get() { return ProtoVector.this.get(index); }
          @Override public int  index() { return index; }
          @Override public void set(double value) { ProtoVector.this.values[index] = value; }
        };
      }

    };
  }

  @Override protected Iterator<Element> iterateNonZero() { return iterator(); }
  @Override protected Matrix matrixLike(int rows, int columns) { return new ProtoMatrix(rows, columns); }
  @Override public boolean isDense() { return true; }
  @Override public boolean isSequentialAccess() { return true; }
  @Override public void mergeUpdates(OrderedIntDoubleMapping updates) {
    // this would only get called if !isAddConstantTime
    throw new UnsupportedOperationException("Default operation");
  }
  @Override public double getQuick(int index) { return values[index]; }
  @Override public Vector like() { return new ProtoVector(values.length); }
  @Override public void setQuick(int index, double value) { values[index] = value; }
  @Override public int getNumNondefaultElements() { return size(); }
  @Override public double getLookupCost() { return 2/*bigger than local cost*/; }
  @Override public double getIteratorAdvanceCost() { return 1; }
  @Override public boolean isAddConstantTime() { return true; }
}
