package com.oxdata.h2o;

import org.apache.mahout.math.Vector;

public class H2OVectorTest extends AbstractVectorTest {
  @Override Vector create(Vector original) {
    return new H2OVector(original);
  }
}
