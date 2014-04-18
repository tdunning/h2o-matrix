package ai.h2o.algo;

import ai.h2o.math.H2OMatrix;
import ai.h2o.math.H2OMatrixTask;
import ai.h2o.math.H2ORow;
import java.util.Random;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.function.DoubleDoubleFunction;
import org.apache.mahout.math.function.DoubleFunction;
import org.apache.mahout.math.function.Functions;
import water.Iced;

public class KMeans extends Iced /*only required because the H2OMatrixTask is a NON-static inner class */{
  // Stopping criteria: max iterations exceeded
  private static final int MAX_ITER=10;
  // Stopping criteria: clusters stopped moving
  private static final double TOLERANCE = 0.001;

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
    H2ORows centroids = new H2ORows(K,cols);
    random_init( centroids );

    for( int iter = 0; iter < MAX_ITER; iter++ ) {
      System.out.println("Iteration " + iter);
      System.out.println(centroids);
      final H2ORows centroids2 = centroids;

      H2ORows next = new H2OMatrixTask<H2ORows>() {
        @Override public H2ORows map(Vector point, H2ORows tmp) {
          int nearest = centroids2.nearest(point);
          if( tmp == null ) tmp = new H2ORows(K, cols);
          tmp.addPoint(nearest,point);
          return tmp;
        }
        @Override public void reduce(H2ORows A, H2ORows B) { A.reduce(B); }
      }.mapreduce((H2OMatrix)_matrix);

      // Sometimes a cluster will end up with NO members, especially if the
      // starting point is very bad.  Reset such clusters to a random row.
      next.fixupEmptyClusters(_matrix);

      // We have summed up the members, now divide by member count, giving a
      // new average cluster center.
      next.divideByMembers();

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
    final long[] _members;
    H2ORows(int K, int cols) {
      _members = new long[K];
      _rows = new H2ORow[K];
      for( int i=0; i<K; i++ )
        _rows[i] = new H2ORow(cols);
    }

    void addPoint( int nearest, Vector point ) {
      _rows[nearest].assign(point, Functions.PLUS);
      _members[nearest]++;
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
        //if( err > tol ) return false; // Early-out check
      }
      System.out.println("Mean error between clusters = "+Math.sqrt(err/_rows[0].size()));
      return err > tol;
    }

    void assign( H2ORows that, DoubleDoubleFunction f ) {
      for( int i=0; i<_rows.length; i++ )
        _rows[i].assign(that._rows[i], f);
    }
    void assign( DoubleFunction f ) {
      for( H2ORow row : _rows )
        row.assign(f);
    }

    void divideByMembers() {
      for( int i=0; i<_rows.length; i++ )
        _rows[i].assign(Functions.mult(1.0/_members[i]));
    }

    // For clusters with no members, inject a random row
    void fixupEmptyClusters(Matrix m) {
      for( int i=0; i<_rows.length; i++ )
        if( _members[i]==0 ) {
          _members[i] = 1;
          int row = new Random().nextInt(m.rowSize());
          _rows[i] = (H2ORow)(new H2ORow(_rows[0].size()).assign(m.viewRow(row)));
        }
    }

    void reduce(H2ORows B) { assign(B,Functions.PLUS); water.util.Utils.add(_members,B._members); }

    @Override public String toString() {
      StringBuilder sb = new StringBuilder();
      for( int k = 0; k < _rows.length; k++ )
        sb.append("Centroid ").append(k).append(": cnt=").append(_members[k]).append(" : ").append(_rows[k]).append("\n");
      return sb.toString();
    }
  }
}
