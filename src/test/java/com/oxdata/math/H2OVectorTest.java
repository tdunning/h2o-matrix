package com.oxdata.math;

import java.util.HashSet;
import org.apache.mahout.math.Vector;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class H2OVectorTest extends AbstractVectorTest {
  @BeforeClass public static void stall() { 
    stall_till_cloudsize(1); 
    water.Scope.enter();
  }
  @AfterClass public static void cleanup() { water.Scope.exit(); }
  @Override Vector create(Vector original) { return new H2OVector(original); }
}
