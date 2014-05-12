package ai.h2o.math;

import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.Functions;
import java.util.HashMap;
import water.*;

/**
 * Implement a distributed RNG generator
 */
public abstract class H2ODoubleFunction extends DoubleFunction implements Freezable {
  protected void setupLocal() { } // Any node-local init

  static final H2ODoubleFunction ABS     = new H2ODoubleFunction() { @Override public double apply( double a ) { return Math.abs(a); } };
  static final H2ODoubleFunction IDENTITY= new H2ODoubleFunction() { @Override public double apply( double a ) { return          a ; } };
  static final H2ODoubleFunction SIN     = new H2ODoubleFunction() { @Override public double apply( double a ) { return Math.sin(a); } };

  static final private HashMap<DoubleFunction,H2ODoubleFunction> FUNCS = new HashMap();
  static {
    FUNCS.put(Functions.ABS     ,ABS     );
    FUNCS.put(Functions.IDENTITY,IDENTITY);
    FUNCS.put(Functions.SIN     ,SIN     );
  }

  static public H2ODoubleFunction map( DoubleFunction df) {
    if( df instanceof H2ODoubleFunction ) return (H2ODoubleFunction)df;
    H2ODoubleFunction h2odf = FUNCS.get(df);
    if( h2odf != null ) return h2odf;
    throw new IllegalArgumentException("H2OMatrix ops only run well with H2ODoubleFunctions; found "+df.getClass());
  }

  // Normally auto-gened by H2O's Weaver, but must inherit from DoubleFunction
  // instead of either Iced or DTask.
  @Override public AutoBuffer write(AutoBuffer bb) { return bb; }
  @Override public Freezable read(AutoBuffer bb) { return this; }
  @Override public AutoBuffer write_impl(AutoBuffer bb) { return bb; }
  @Override public Freezable read_impl(AutoBuffer bb) { return this; }
  @Override public AutoBuffer writeJSON(AutoBuffer bb) { return bb; }
  @Override public Freezable readJSON(AutoBuffer bb) { return this; }
  @Override public H2ODoubleFunction clone() { return this; }
  private static int _frozen$type;
  @Override public int frozenType() {
    return _frozen$type == 0 ? (_frozen$type=water.TypeMap.onIce(H2ODoubleFunction.class.getName())) : _frozen$type;
  }
}
