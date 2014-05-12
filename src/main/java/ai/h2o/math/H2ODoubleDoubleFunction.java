package ai.h2o.math;

import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.Functions;
import java.util.HashMap;
import water.*;

/**
 * Implement a distributed RNG generator
 */
public abstract class H2ODoubleDoubleFunction extends DoubleDoubleFunction implements Freezable {
  protected void setupLocal() { } // Any node-local init

  static final H2ODoubleDoubleFunction MINUS = new H2ODoubleDoubleFunction() { @Override public double apply( double a, double b ) { return a-b; } };
  static final H2ODoubleDoubleFunction MULT  = new H2ODoubleDoubleFunction() { @Override public double apply( double a, double b ) { return a*b; } };
  static final H2ODoubleDoubleFunction PLUS  = new H2ODoubleDoubleFunction() { @Override public double apply( double a, double b ) { return a+b; } };

  static final private HashMap<DoubleDoubleFunction,H2ODoubleDoubleFunction> FUNCS = new HashMap();
  static {
    FUNCS.put(Functions.MINUS ,MINUS );
    FUNCS.put(Functions.MULT  ,MULT  );
    FUNCS.put(Functions.PLUS  ,PLUS  );
  }

  static public H2ODoubleDoubleFunction map( DoubleDoubleFunction ddf) {
    if( ddf instanceof H2ODoubleDoubleFunction ) return (H2ODoubleDoubleFunction)ddf;
    H2ODoubleDoubleFunction h2oddf = FUNCS.get(ddf);
    if( h2oddf != null ) return h2oddf;
    throw new IllegalArgumentException("H2OMatrix ops only run well with H2ODoubleFunctions; found "+ddf.getClass());
  }

  // Normally auto-gened by H2O's Weaver, but must inherit from DoubleDoubleFunction
  // instead of either Iced or DTask.
  @Override public AutoBuffer write(AutoBuffer bb) { return bb; }
  @Override public Freezable read(AutoBuffer bb) { return this; }
  @Override public AutoBuffer write_impl(AutoBuffer bb) { return bb; }
  @Override public Freezable read_impl(AutoBuffer bb) { return this; }
  @Override public AutoBuffer writeJSON(AutoBuffer bb) { return bb; }
  @Override public Freezable readJSON(AutoBuffer bb) { return this; }
  public void copyOver(Freezable that) { }
  @Override public H2ODoubleDoubleFunction clone() { return this; }
  private static int _frozen$type;
  @Override public int frozenType() {
    return _frozen$type == 0 ? (_frozen$type=water.TypeMap.onIce(H2ODoubleDoubleFunction.class.getName())) : _frozen$type;
  }
}
