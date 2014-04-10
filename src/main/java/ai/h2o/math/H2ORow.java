package ai.h2o.math;

import java.util.Iterator;

import org.apache.mahout.math.*;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;

import water.*;
import water.fvec.*;
import water.fvec.Vec.VectorGroup;

/*
 * H2ORow is a type of H2OVector which represents a row
 * in a given matrix.
 *
 * H2ORow is always associated with a given matrix/row
 * and cannot have a standalone existence.
 */
public class H2ORow extends H2OVector {
  H2OMatrix _matrix;
  int _row;
  double cached[];
  int _minidx;
  int _maxidx;

  public H2ORow (H2OMatrix matrix, int row) {
    super(matrix.columnSize());
    _matrix = matrix;
    _row = row;
    cached = new double[matrix.columnSize()];

    for (int i = 0; i < matrix.columnSize(); i++) {
      double val = _matrix.getQuick(row, i);
      cached[i] = val;
      if (cached[i] < cached[_minidx])
        _minidx = i;
      if (cached[i] > cached[_maxidx])
        _maxidx = i;
    }
  }

  @Override public double minValue() { return cached[_minidx]; }
  @Override public double maxValue() { return cached[_maxidx]; }

  @Override protected Iterator<Element> iterator() {
    return new Iterator<Element>() {
      private int _i=-1, _len=(int)_matrix.columnSize();
      private Element _elem = new Element() {
          @Override public double get() { return getQuick(_i); }
          @Override public int index() { return _i; }
          @Override public void set(double value) { setQuick(_i, value); }
        };
      @Override public boolean hasNext() { return _i+1<_len; }
      @Override public Element next() { _i++; return _elem; }
      @Override public void remove() { throw H2O.fail(); }
    };
  }

  @Override public void setQuick(int index, double value) {
    _matrix.setQuick(_row, index, value);
    cached[index] = value;
    if (value < cached[_minidx])
      _minidx = index;
    if (value > cached[_maxidx])
      _maxidx = index;
  }

  @Override public double getQuick(int index) {
    return cached[index];
  }
}
