package com.tdunning.math;

import org.apache.mahout.math.AbstractMatrix;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.MatrixSlice;
import org.apache.mahout.math.Vector;

/**
 * Implement a simple matrix type to emulate what an h2o based matrix would need.
 */
public class ProtoMatrix extends AbstractMatrix {
    private final double[] values;

    public ProtoMatrix(int rows, int columns) {
        super(rows, columns);
        values = new double[rows * columns];
    }

    public ProtoMatrix(Matrix original) {
        super(original.rowSize(), original.columnSize());
        int columns = original.columnSize();
        values = new double[original.rowSize() * columns];
        for (MatrixSlice row : original) {
            int rowBase = row.index() * columns;
            for (Vector.Element element : row.all()) {
                values[rowBase + element.index()] = element.get();
            }
        }
    }

    @Override
    public Matrix assignColumn(int column, Vector other) {
        for (int i = 0; i < rows; i++) {
            int rowBase = i * columns;
            values[rowBase + column] = other.getQuick(i);
        }
        return this;
    }

    @Override
    public Matrix assignRow(int row, Vector other) {
        int rowBase = row * columns;
        for (int i = 0; i < columns; i++) {
            values[rowBase + i] = other.getQuick(i);
        }
        return this;
    }

    @Override
    public double getQuick(int row, int column) {
        return values[row * columns + column];
    }

    @Override
    public Matrix like() {
        return new ProtoMatrix(this);
    }

    @Override
    public Matrix like(int rows, int columns) {
        return new ProtoMatrix(this.rows, this.columns);
    }

    @Override
    public void setQuick(int row, int column, double value) {
        values[row * columns + column] = value;
    }
}
