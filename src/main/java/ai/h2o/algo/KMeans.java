package ai.h2o.algo;

import ai.h2o.math.H2OMatrix;
import ai.h2o.math.H2OMatrixTask;
import ai.h2o.math.H2ORow;
import java.io.File;
import java.util.Arrays;
import java.util.Random;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import water.Iced;

public class KMeans extends Iced /*only required because the H2OMatrixTask is a NON-static inner class */{
  // Stopping criteria: max iterations exceeded
  private final int MAX_ITER=10;
  // Stopping criteria: clusters stopped moving
  private final double TOLERANCE = 0.001;

  transient final Matrix _matrix;

  public KMeans(Matrix m) { _matrix = m; }

  private void random_init( H2ORows centroids ) {
    Random R = new Random((int)System.currentTimeMillis());
    int ndim = _matrix.columnSize();
    double[] mins = new double[ndim];
    double[] maxs = new double[ndim];

    for( int c = 0; c < ndim; c++ ) {
      mins[c] = _matrix.viewColumn(c).minValue();
      maxs[c] = _matrix.viewColumn(c).maxValue();
    }

    for( int k = 0; k < centroids._rows.length; k++ )
      for( int c = 0; c < ndim; c++ )
        centroids._rows[k].setQuick(c, mins[c] + (maxs[c]-mins[c])*R.nextDouble());
  }

  private static double sq(double X) { return X * X; }

  private static double calc_sqdist( H2ORow pointA, Vector pointB ) {
    double dist = 0;
    int len = pointA.size();
    for( int i = 0; i < len; i++ )
      dist += sq(pointA.getQuick(i) - pointB.getQuick(i));
    return dist;
  }

  public KMeans run(final int K) {
    final int cols = _matrix.columnSize();
    final int rows = _matrix.rowSize();
    H2ORows centroids = new H2ORows(K,cols);
    random_init( centroids );

    for( int iter = 0; iter < MAX_ITER; iter++ ) {
      System.out.println ("Iteration " + iter);
      System.out.println(centroids);
      final H2ORows centroids2 = centroids;

      H2ORows next = new H2OMatrixTask<H2ORows>() {
        @Override public H2ORows map(Vector point, H2ORows tmp) {
          int nearest = centroids2.nearest(point);
          if( tmp == null ) tmp = new H2ORows(K, cols);
          tmp._rows[nearest].plus(point);
          return tmp;
        }

        @Override public void reduce(H2ORows A, H2ORows B) { A.plus(B); }
      }.mapreduce((H2OMatrix)_matrix);

      next.divide(rows);

      // Stopping criteria scales by dimensions.
      if( !centroids.distance_exceeds(next, TOLERANCE * cols) )
        break;
      centroids = next;
    }
    return this;
  }

  // A collection of H2ORows; very nearly a Matrix
  private static class H2ORows extends Iced {
    final H2ORow[] _rows;
    H2ORows(int K, int cols) {
      _rows = new H2ORow[K];
      for( int i=0; i<K; i++ )
        _rows[i] = new H2ORow(cols);
    }

    // Index of nearest row to this point (smallest Euclidean distance)
    int nearest( Vector point ) {
      int nearest_idx = 0;
      double nearest_dist = calc_sqdist(_rows[nearest_idx], point);
      for( int i = 1; i < _rows.length; i++ ) {
        double dist = calc_sqdist(_rows[i], point);
        if( dist < nearest_dist ) {
          nearest_idx = i;
          nearest_dist = dist;
        }
      }
      return nearest_idx;
    }

    // TRUE if the Euclidean distance between two H2ORows exceeds tolerance
    boolean distance_exceeds( H2ORows rs, double tol ) {
      double err=0;
      for( int i = 0; i < _rows.length; i++ ) {
        err += calc_sqdist(_rows[i],rs._rows[i]);
        if( err > tol ) return false; // Early-out check
      }
      return true;
    }


    void plus( H2ORows rows ) { for( int i=0; i<_rows.length; i++ ) _rows[i].plus(rows._rows[i]); }
    void divide( double d )   { for( int i=0; i<_rows.length; i++ ) _rows[i].divide(d); }

    @Override public String toString() {
      StringBuilder sb = new StringBuilder();
      for( int k = 0; k < _rows.length; k++ )
        sb.append("Centroid ").append(k).append(": ").append(_rows[k]).append("\n");
      return sb.toString();
    }
  }
}
