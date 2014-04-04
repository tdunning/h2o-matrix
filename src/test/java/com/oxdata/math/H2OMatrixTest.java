package com.oxdata.math;

import org.apache.mahout.math.Matrix;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.util.HashSet;

public class H2OMatrixTest extends AbstractMatrixTest {
  @BeforeClass public static void stall() { 
    stall_till_cloudsize(2); 
    water.Scope.enter();
  }
  @AfterClass public static void cleanup() { water.Scope.exit(); }

  @Override Matrix create(Matrix original) { return new H2OMatrix(original); }
  @Override Matrix create(int rows, int columns) { return new H2OMatrix(rows, columns); }
}
