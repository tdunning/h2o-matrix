package com.oxdata.math;

import org.apache.mahout.math.function.DoubleDoubleFunction;

import water.*;

/**
 * Implement a distributed RNG generator
 */
public abstract class H2ODoubleDoubleFunction extends DoubleDoubleFunction implements Freezable {
  protected void setupLocal() { } // Any node-local init

  static final H2ODoubleDoubleFunction MINUS = new H2ODoubleDoubleFunction() { @Override public double apply( double a, double b ) { return a-b; } };


  // Normally auto-gened by H2O's Weaver, but must inherit from DoubleDoubleFunction
  // instead of either Iced or DTask.
  @Override public AutoBuffer write(AutoBuffer bb) { return bb; }
  @Override public H2ODoubleDoubleFunction read(AutoBuffer bb) { return this; }
  @Override public H2ODoubleDoubleFunction newInstance() { throw H2O.fail(); }
  private static int _frozen$type;
  @Override public int frozenType() {
    return _frozen$type == 0 ? (_frozen$type=water.TypeMap.onIce(H2ODoubleDoubleFunction.class.getName())) : _frozen$type;
  }
  @Override public AutoBuffer writeJSONFields(AutoBuffer bb) { return bb; }
  @Override public water.api.DocGen.FieldDoc[] toDocField() { return null; }
}
