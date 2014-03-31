package com.oxdata.h2o;

import org.apache.mahout.math.Matrix;

public class ProtoMatrixTest extends AbstractMatrixTest {
  @Override Matrix create(Matrix original) {
    return new ProtoMatrix(original);
  }

  @Override Matrix create(int rows, int columns) {
    return new ProtoMatrix(rows, columns);
  }
}
