package ai.h2o.algo;

import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;
import java.io.File;
import java.util.Random;
import java.util.Arrays;

import ai.h2o.math.H2OMatrix;
import ai.h2o.math.H2OMatrixTask;
import water.Iced;

public class KMeans extends Iced /*only required because the H2OMatrixTask is a NON-static inner class */{
  transient Matrix matrix;
  transient Random generator;

  KMeans(Matrix m) {
    matrix = m;

    generator = new Random((int) (System.currentTimeMillis()));
  }

  void random_init (double[][] centroids) {
    double[] mins = new double[matrix.columnSize()];
    double[] maxs = new double[matrix.columnSize()];

    for (int c = 0; c < matrix.columnSize(); c++) {
      mins[c] = matrix.viewColumn(c).minValue();
      maxs[c] = matrix.viewColumn(c).maxValue();
    }

    for (int k = 0; k < centroids.length; k++) {
      for (int c = 0; c < centroids[0].length; c++) {
        centroids[k][c] = mins[c] + (generator.nextDouble() * (maxs[c]-mins[c]));
      }
    }
  }

  void zero_init (double[][] centroids) {
    for (double[] centroid : centroids)
      Arrays.fill (centroid, 0.0);
  }

  double sq(double X) { return X * X; }

  double calc_sqdist (double[] pointA, Vector pointB) {
    double dist = 0;
    for (int i = 0; i < pointA.length; i++)
      dist += sq(pointA[i] - pointB.getQuick(i));

    return dist;
  }

  int nearest_centroid (double[][] centroids, Vector point) {
    int nearest_idx = 0;
    double nearest_dist = 0;

    for (int i = 0; i < centroids.length; i++) {
      double dist = calc_sqdist (centroids[i], point);

      if ((i == 0) || (dist < nearest_dist)) {
        nearest_idx = i;
        nearest_dist = dist;
      }
    }

    return nearest_idx;
  }

  boolean same (double[][] X, double[][] Y) {
    for (int i = 0; i < X.length; i++)
      for (int j = 0; j < X[0].length; j++)
        if (X[i][j] != Y[i][j])
          return false;
    return true;
  }

  void assign (double[][] X, double[][] Y) {
    for (int i = 0; i < Y.length; i++)
      System.arraycopy (Y[i], 0, X[i], 0, Y[i].length);
  }

  void print_centroids (double[][] centroids) {
    for (int k = 0; k < centroids.length; k++) {
      String output = "Centroid " + k + ": [";
      for (int c = 0; c < centroids[0].length; c++) {
        if (c > 0)
          output = output + ", ";
        output += centroids[k][c];
      }
      output += "]";

      System.out.println (output);
    }
  }

  public void run(final int K) {
    final int cols = matrix.columnSize();
    final int rows = matrix.rowSize();
    double[][] centroids = new double[K][cols];
    double[][] next_sums = new double[K][cols];
    double[][] next_means = new double[K][cols];

    random_init (centroids);

    for (int iter = 0; true; iter++) {
      final double[][] iter_centroids = centroids;
      zero_init (next_means);
      zero_init (next_sums);

      System.out.println ("Iteration " + iter);
      print_centroids(centroids);

      /*
      for (Vector point : matrix) {
        int nearest = nearest_centroid (centroids, point);

        for (int c = 0; c < cols; c++)
          next_sums[nearest][c] += point.getQuick(c);
      }
      */

      next_sums = new H2OMatrixTask<Crunk>() {
        public Crunk map(Vector point) {
          int nearest = nearest_centroid (iter_centroids, point);
          double next_sum[][] = new double[K][cols];
          for (int c = 0; c < cols; c++)
            next_sum[nearest][c] += point.getQuick(c);
          return new Crunk(next_sum);
        }

        public Crunk reduce(Crunk A, Crunk B) {
          for (int k = 0; k < A._ds.length; k++)
            for (int c = 0; c < A._ds[k].length; c++)
              A._ds[k][c] += B._ds[k][c];

          return A;
        }
      }.mapreduce((H2OMatrix)matrix)._ds;

      for (int k = 0; k < K; k++)
        for (int c = 0; c < cols; c++)
          next_means[k][c] = next_sums[k][c]/rows;

      if (same (centroids, next_means))
        break;

      assign(centroids, next_means);
    }
  }

  static private class Crunk extends Iced { public final double _ds[][]; Crunk(double ds[][]) { _ds=ds; }}
}
