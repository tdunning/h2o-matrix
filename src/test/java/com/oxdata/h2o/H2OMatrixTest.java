package com.oxdata.h2o;

import org.apache.mahout.math.Matrix;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.HashSet;

public class H2OMatrixTest extends AbstractMatrixTest {
  @BeforeClass public static void stall() { stall_till_cloudsize(1); }

  static HashSet<H2OMatrix> _mats = new HashSet<>();
  @AfterClass public static void cleanup() {
    for( H2OMatrix mat : _mats )
      mat.delete();
  }

  @Override Matrix create(Matrix original) {
    H2OMatrix mat = new H2OMatrix(original);
    _mats.add(mat);
    return mat;
  }

  @Override Matrix create(int rows, int columns) {
    H2OMatrix mat = new H2OMatrix(rows, columns);
    _mats.add(mat);
    return mat;
  }
}
