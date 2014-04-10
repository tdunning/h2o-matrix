package ai.h2o.math;

import java.util.Iterator;
import org.apache.mahout.math.*;

/*
 * H2OVector is an abstract class in front of either H2ORow or H2OColumn.
 *
 * H2OColumn is a more "general purpose" vector.
 *
 * H2ORow is a convenience class to make a row of a matrix appear like
 * a virtual Vector.
 */
public abstract class H2OVector extends AbstractVector {
  public H2OVector(int size) { super(size); }

  @Override protected Matrix matrixLike(int rows, int columns) { return new H2OMatrix(rows, columns); }
  @Override protected Iterator<Element> iterateNonZero() { return iterator(); }
  @Override public boolean isAddConstantTime() { return true; }
  @Override public double getIteratorAdvanceCost() { return 1; }
  @Override public double getLookupCost() { return 2/*bigger than local cost*/; }
  // Number of known-non-zeros
  @Override public int getNumNondefaultElements() { return (minValue()==0 && maxValue()==0) ? 0 : size(); }
  // A new copy is always a column vector. Row vectors have no standalone identity.
  @Override public Vector like() { return new H2OColumn(size()); }
  @Override public void mergeUpdates(OrderedIntDoubleMapping updates) {
    // this would only get called if !isAddConstantTime
    throw new UnsupportedOperationException("Default operation");
  }
  @Override public boolean isSequentialAccess() { return false; } // Random access is fast
  @Override public boolean isDense() { return true; }

}
