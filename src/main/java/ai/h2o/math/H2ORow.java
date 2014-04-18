package ai.h2o.math;

import java.util.Arrays;
import java.util.Iterator;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.Functions;
import water.*;

/*
 * H2ORow is a type of H2OVector which represents a row in a given matrix.
 *
 * H2ORow can be associated with a given matrix/row or have a standalone existence.
 */
public class H2ORow extends H2OVector implements Freezable {
  H2OMatrix _matrix;      // Backing matrix, or NULL
  int _row;
  double _cached[];
  transient double _min, _max;  // min/max cached; NaN means not-computed

  // An H2ORow backed by an H2OMatrix
  public H2ORow (H2OMatrix matrix, int row) {
    super(matrix.columnSize());
    _matrix = matrix;
    _row = row;
    _cached = new double[matrix.columnSize()];
    for( int i = 0; i < _cached.length; i++ )
      _cached[i] = _matrix.getQuick(row, i);
    _min = _max = Double.NaN;   // min/max not computed
  }

  // A standalone H2ORow
  public H2ORow(int width) {
    super(width);
    _cached = new double[width];
    _matrix = null;
    _row = -1;
    // min/max already correctly zero
  }

  private void compute_minmax() {
    if( _min == Double.NaN ) compute_minmax_impl();
  }
  private void compute_minmax_impl() {
    if( _cached.length==0 ) return; // min/max of zero elements is NaN
    double min =  Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;
    for( double val : _cached ) {
      if( val < min ) min = val;
      if( val > max ) max = val;
    }
    _min = min;
    _max = max;
  }

  @Override public double minValue() { compute_minmax(); return _min; }
  @Override public double maxValue() { compute_minmax(); return _max; }

  @Override protected Iterator<Element> iterator() {
    return new Iterator<Element>() {
      private int _i=-1, _len=_cached.length;
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
    if( _matrix != null ) 
      _matrix.setQuick(_row, index, value);
    double old = _cached[index];
    _cached[index] = value;
    if( value < _min ) _min = value; // Update min/max value
    else if( value > _max ) _max = value;
    else if( _min == old || old == _max )
      _min = _max = Double.NaN; // Flush min/max cache
  }

  @Override public double getQuick(int index) { return _cached[index]; }

  @Override public H2ORow assign( double d ) {
    if( _matrix != null ) _matrix.assign(d);
    Arrays.fill(_cached, d);
    return this;
  }

  @Override public Vector assign( DoubleFunction f ) {
    if( _matrix != null ) return super.assign(f);
    _min = _max = Double.NaN;   // Reset cache
    for( int i=0; i<_cached.length; i++ )
      _cached[i] = f.apply(_cached[i]);
    return this;
  }

  @Override public Vector assign( Vector that, DoubleDoubleFunction f ) {
    if( _matrix != null ) return super.assign(that,f);
    _min = _max = Double.NaN;   // Reset cache
    if( f == Functions.PLUS ) {
      for( int i=0; i<_cached.length; i++ )
        _cached[i] += that.getQuick(i);
    } else {
      return super.assign(that,f);
    }
    return this;
  }

  // ---
  // Normally auto-gened by H2O's Weaver, but must inherit from AbstractMatrix
  // instead of either Iced or DTask.
  @Override public AutoBuffer write(AutoBuffer bb) { return bb.put(_matrix).put4(_row).putA8d(_cached); }
  @Override public H2ORow read(AutoBuffer bb) {
    _matrix = bb.get(H2OMatrix.class);
    _row = bb.get4();
    _cached = bb.getA8d();
    _min = _max = Double.NaN;
    return this;
  }
  public H2ORow(){super(0);}
  public void copyOver( Freezable that ) { 
    H2ORow row = (H2ORow)that;
    _matrix = row._matrix;
    _row = row._row;
    _cached = row._cached;
    _min = row._min;
    _max = row._max;
  }
  @Override public H2ORow newInstance() { return new H2ORow(0); }
  private static int _frozen$type;
  @Override public int frozenType() {
    return _frozen$type == 0 ? (_frozen$type=water.TypeMap.onIce(H2ORow.class.getName())) : _frozen$type;
  }
  @Override public AutoBuffer writeJSONFields(AutoBuffer bb) { return bb; }
  @Override public water.api.DocGen.FieldDoc[] toDocField() { return null; }
  // ---
}
