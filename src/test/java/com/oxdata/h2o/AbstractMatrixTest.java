package com.oxdata.h2o;

import org.apache.mahout.math.DenseMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.MatrixSlice;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.function.Functions;
import org.apache.mahout.math.function.VectorFunction;
import org.apache.mahout.math.jet.random.Normal;
import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.assertEquals;

public abstract class AbstractMatrixTest extends water.TestUtil {
    abstract Matrix create(Matrix original);
    abstract Matrix create(int rows, int columns);

    @Test
    public void testBasicAggregation() {
        Matrix r = new DenseMatrix(1031, 5131)
                .assign(new Normal(0,1,new Random()));

        Matrix m = create(r);

        compareMatrices(r, m);
        compareMatrices(r, m.like());
    }

    private void compareMatrices(Matrix r, Matrix m) {
        assertEquals(r.rowSize(), m.rowSize());
        assertEquals(r.columnSize(), m.columnSize());

        assertEquals(0, r.minus(m).aggregate(Functions.PLUS, Functions.ABS), 0);
        assertEquals(0, m.minus(r).aggregate(Functions.PLUS, Functions.ABS), 0);
        assertEquals(m.aggregate(Functions.PLUS, Functions.ABS), r.aggregate(Functions.PLUS, Functions.ABS), 0);

        VectorFunction sum = new VectorFunction() {
            @Override
            public double apply(Vector f) {
                return f.zSum();
            }
        };

        assertEquals(r.aggregateRows(sum).aggregate(Functions.MULT, Functions.SIN),
                m.aggregateRows(sum).aggregate(Functions.MULT, Functions.SIN),
                0);

        assertEquals(r.aggregateColumns(sum).aggregate(Functions.MULT, Functions.SIN),
                m.aggregateColumns(sum).aggregate(Functions.MULT, Functions.SIN),
                0);
    }

    @Test
    public void testViews() {
        Matrix r = new DenseMatrix(1031, 5131)
                .assign(new Normal(0,1,new Random()));

        Matrix m = create(r);

        Random gen = new Random();
        for (int i = 0; i < 20; i++) {
            int row = gen.nextInt(m.columnSize());
            assertEquals(r.viewColumn(row).zSum(), m.viewColumn(row).zSum(), 0);

            int column = gen.nextInt(m.columnSize());
            assertEquals(r.viewColumn(column ).zSum(), m.viewColumn(column ).zSum(), 0);
        }
    }

    @Test
    public void testAssign() {
        Matrix r = new DenseMatrix(1031, 5131)
                .assign(new Normal(0,1,new Random()));

        Matrix m1 = create(r.rowSize(), r.columnSize());
        Matrix m2 = m1.like(r.rowSize(), r.columnSize());

        for (MatrixSlice row : r) {
            m1.assignRow(row.index(), row.vector());
        }
        compareMatrices(r, m1);

        for (int i = 0; i < r.columnSize(); i++) {
            m2.assignColumn(i, r.viewColumn(i));
        }
        compareMatrices(r, m2);
    }
}
