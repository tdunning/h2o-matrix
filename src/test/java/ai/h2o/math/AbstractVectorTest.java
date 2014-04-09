package ai.h2o.math;

import java.util.Random;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.Functions;
import org.apache.mahout.math.jet.random.Normal;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractVectorTest extends water.TestUtil {
  abstract Vector create(Vector original);

  @Test
  public void testBasics() {
    Vector r = new DenseVector(1023).assign(new Normal(0, 1, new Random()));
    Vector m = create(r);
    
    assertEquals(r.zSum(), m.zSum(), 0);
    assertEquals(r.aggregate(Functions.PLUS, Functions.SIN), m.aggregate(Functions.PLUS, Functions.SIN), 0);
    
    r.assign(0);
    for (Vector.Element element : m.all()) {
      r.set(element.index(), element.get());
    }
    assertEquals(r.zSum(), m.zSum(), 0);
    assertEquals(r.aggregate(Functions.PLUS, Functions.SIN), m.aggregate(Functions.PLUS, Functions.SIN), 0);
  }

  @Test
  public void testProperties() {
    Vector r = new DenseVector(1023).assign(new Normal(0, 1, new Random()));
    Vector m = create(r);

    assertTrue(m.isAddConstantTime());
    assertTrue(m.isDense());
    assertTrue(m.isSequentialAccess());

    assertEquals(m.size(), m.getNumNondefaultElements());

    m.set(31, -5);
    r.set(31, 0);

    assertEquals(r.zSum(), m.zSum() + 5, 1e-13);
  }

  @Test
  public void testFancyAssign() {
    final Random gen = new Random();
    Vector r = new SequentialAccessSparseVector(1023).
      assign( new DoubleFunction() {
          @Override public double apply(double x) {
            return gen.nextDouble() < 0.1 ? gen.nextGaussian() : 0;
          }
        });

    Vector m = create(r).assign(new Normal(0, 1, new Random()));

    r.assign(m);

    assertEquals(0, r.minus(m).norm(1), 0);
  }
}
