package com.oxdata.math;

/**
 * Implement a distributed RNG generator
 */
public class H2ONormal extends H2ODoubleFunction {
  final double _mean, _std;
  final long _seed;
  transient java.util.Random _rng;
  H2ONormal( double mean, double std, java.util.Random RNG ) { _mean = mean; _std=std; _seed = RNG.nextLong(); }

  @Override public double apply( double d ) {
    return _rng.nextGaussian()*_std+_mean;
  }
  // Set the node-shared RNG per-node from the seed
  @Override protected void setupLocal() { _rng = new java.util.Random(_seed*(1+water.H2O.SELF.index())); }
}
