package com.oxdata.h2o;

import org.apache.mahout.math.Matrix;

public class H2OMatrixTest extends AbstractMatrixTest {
  @Override Matrix create(Matrix original) {
    return new H2OMatrix(original);
  }

  @Override Matrix create(int rows, int columns) {
    return new H2OMatrix(rows, columns);
  }
}
