package com.oxdata.h2o;

import org.apache.mahout.math.Vector;

public class ProtoVectorTest extends AbstractVectorTest {
  @Override Vector create(Vector original) {
    return new ProtoVector(original);
  }
}
