package com.oxdata.math;

import org.apache.mahout.math.function.DoubleFunction;

import water.*;

/**
 * Implement a distributed RNG generator
 */
public abstract class H2ODoubleFunction extends DoubleFunction implements Freezable {
  protected void setupLocal() { } // Any node-local init

  // Normally auto-gened by H2O's Weaver, but must inherit from DoubleFunction
  // instead of either Iced or DTask.
  @Override public AutoBuffer write(AutoBuffer bb) { return bb; }
  @Override public H2ODoubleFunction read(AutoBuffer bb) { return this; }
  @Override public H2ODoubleFunction newInstance() { throw H2O.fail(); }
  private static int _frozen$type;
  @Override public int frozenType() {
    return _frozen$type == 0 ? (_frozen$type=water.TypeMap.onIce(H2ODoubleFunction.class.getName())) : _frozen$type;
  }
  @Override public AutoBuffer writeJSONFields(AutoBuffer bb) { return bb; }
  @Override public water.api.DocGen.FieldDoc[] toDocField() { return null; }
}
