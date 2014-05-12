package ai.h2o.math;

import java.util.HashSet;
import org.apache.mahout.math.Vector;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class H2OColumnTest extends AbstractVectorTest {
  @BeforeClass public static void stall() { 
    stall_till_cloudsize(1); 
    Scope.enter();
  }
  @AfterClass public static void cleanup() { Scope.exit(); }
  @Override Vector create(Vector original) { return new H2OColumn(original); }
}
