package com.oxdata.h2o;

import java.util.HashSet;
import org.apache.mahout.math.Vector;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class H2OVectorTest extends AbstractVectorTest {
  @BeforeClass public static void stall() { stall_till_cloudsize(1); }

  static HashSet<H2OVector> _vecs = new HashSet<H2OVector>();
  @AfterClass public static void cleanup() {
    for( H2OVector vec : _vecs )
      vec.delete();
  }
  
  @Override Vector create(Vector original) {
    H2OVector vec = new H2OVector(original);
    _vecs.add(vec);
    return vec;
  }
}
