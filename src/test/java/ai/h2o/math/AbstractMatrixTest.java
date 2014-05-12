package ai.h2o.math;

import ai.h2o.algo.KMeans;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.function.Functions;
import org.apache.mahout.math.function.VectorFunction;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;
import static org.junit.Assert.assertEquals;

public abstract class AbstractMatrixTest extends water.TestUtil {

  abstract Matrix create(Matrix original);
  abstract Matrix create(int rows, int columns);

  @Test
  public void testBasicAggregation() {
    try {
      Scope.enter();
      Matrix r = new H2OMatrix(5131, 1031).assign(new H2ONormal(0,1,new Random()));
      Matrix m = create(r);
      compareMatrices(r, m);
      compareMatrices(r, m.like());
    } finally {
      Scope.exit();
    }
  }

  private void compareMatrices(Matrix r, Matrix m) {
    assertEquals(r.rowSize(), m.rowSize());
    assertEquals(r.columnSize(), m.columnSize());
    assertEquals(0, r.minus(m).aggregate(Functions.PLUS, Functions.ABS), 0);
    assertEquals(0, m.minus(r).aggregate(Functions.PLUS, Functions.ABS), 0);
    assertEquals(m.aggregate(Functions.PLUS, Functions.ABS), r.aggregate(Functions.PLUS, Functions.ABS), 0);
    VectorFunction sum = new ZSum();
    assertEquals(r.aggregateRows(sum).aggregate(Functions.MULT, Functions.SIN),
                 m.aggregateRows(sum).aggregate(Functions.MULT, Functions.SIN),
                 0);
    assertEquals(r.aggregateColumns(sum).aggregate(Functions.MULT, Functions.SIN),
                 m.aggregateColumns(sum).aggregate(Functions.MULT, Functions.SIN),
                 0);
  }
  // Named class instead of anonymous function, so can make a static class, so
  // do not attempt to serialize the outer-class instance.
  private static class ZSum extends H2OVectorFunction {
    @Override public double apply(Vector f) { return f.zSum(); }
  }

  @Test
  public void testViews() {
    try {
      Scope.enter();
      Matrix r = new H2OMatrix(5131, 1031).assign(new H2ONormal(0,1,new Random()));
      Matrix m = create(r);
      Random gen = new Random();
      for (int i = 0; i < 20; i++) {
        int row = gen.nextInt(m.columnSize());
        assertEquals(r.viewColumn(row).zSum(), m.viewColumn(row).zSum(), 0);
        int column = gen.nextInt(m.columnSize());
        assertEquals(r.viewColumn(column ).zSum(), m.viewColumn(column ).zSum(), 0);
      }
    } finally {
      Scope.exit();
    }
  }

  @Test
  public void testAssign() {
    try {
      Scope.enter();
      Matrix r = new H2OMatrix(5131, 1031).assign(new H2ONormal(0,1,new Random()));
      Matrix m1 = create (r.rowSize(), r.columnSize());
      Matrix m2 = m1.like(r.rowSize(), r.columnSize());
      // This one is going to be slow in H2O - the assignment is the "wrong way"
      //for (MatrixSlice row : r)
      //  m1.assignRow(row.index(), row.vector());
      //compareMatrices(r, m1);
      for (int i = 0; i < r.columnSize(); i++)
        m2.assignColumn(i, r.viewColumn(i));
      compareMatrices(r, m2);
    } finally {
      Scope.exit();
    }
  }

  @Test
  public void testFile() throws IOException {
    try {
      Scope.enter();
      H2OMatrix m = new H2OMatrix(new java.io.File("src/test/java/ai/h2o/data/iris.csv"));
      KMeans kmeans = new KMeans(m).run(3);
      System.out.println(kmeans);
    } finally {
      Scope.exit();
    }
  }
}
