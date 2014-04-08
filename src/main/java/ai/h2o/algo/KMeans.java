package ai.h2o.algo;

import ai.h2o.math.H2OMatrix;
import java.io.File;

public class KMeans {
    H2OMatrix matrix;

    KMeans(File csvfile) {
	System.out.println("Loading data file: " + csvfile);
	matrix = new H2OMatrix(csvfile);
    }

    void random_init (double[][] centroids) {
	double[] mins = new double[matrix.columnSize()];
	double[] maxs = new double[matrix.columnSize()];

	for (int c = 0; c < matrix.columnSize(); c++)
	    mins[c] = matrix.getQuick (0, c);

	for (int r = 0; r < matrix.rowSize(); r++) {
	    for (int c = 0; c < matrix.columnSize(); c++) {
		double val = matrix.getQuick (r, c);

		if (val < mins[c]) mins[c] = val;
		if (val > maxs[c]) maxs[c] = val;
	    }
	}

	for (int k = 0; k < centroids.length; k++) {
	    for (int c = 0; c < centroids[0].length; c++) {
		centroids[k][c] = mins[c] + (k * (maxs[c]-mins[c]) / centroids.length);
	    }
	}
    }

    void zero_init (double[][] centroids) {
	for (int i = 0; i < centroids.length; i++) {
	    for (int j = 0; j < centroids[0].length; j++) {
		centroids[i][j] = 0;
	    }
	}
    }

    void fill_row (int row, double[] point) {
	for (int i = 0; i < matrix.columnSize(); i++)
	    point[i] = matrix.getQuick (row, i);
    }

    double sq(double X) { return X * X; }

    double calc_sqdist (double[] pointA, double[] pointB) {
	double dist = 0;
	for (int i = 0; i < pointA.length; i++) {
	    dist += sq(pointA[i] - pointB[i]);
	}

	return dist;
    }

    int nearest_centroid (double[][] centroids, double[] point) {
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
	for (int i = 0; i < X.length; i++) {
	    for (int j = 0; j < X[0].length; j++) {
		if (X[i][j] != Y[i][j])
		    return false;
	    }
	}

	return true;
    }

    void assign (double[][] X, double[][] Y) {
	for (int i = 0; i < X.length; i++)
	    for (int j = 0; j < X[0].length; j++)
		X[i][j] = Y[i][j];
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


    public void run(int K) {
	int cols = matrix.columnSize();
	int rows = matrix.rowSize();
	double[][] centroids = new double[K][cols];
	double[][] next_sums = new double[K][cols];
	double[][] next_means = new double[K][cols];
	double[] point = new double[cols];

	random_init (centroids);

	for (int iter = 0; true; iter++) {
	    zero_init (next_means);
	    zero_init (next_sums);

	    System.out.println ("Iteration " + iter);
	    print_centroids(centroids);

	    for (int r = 0; r < rows; r++) {
		fill_row (r, point);

		int nearest = nearest_centroid (centroids, point);

		for (int c = 0; c < cols; c++) {
		    next_sums[nearest][c] += point[c];
		}
	    }

	    for (int k = 0; k < K; k++)
		for (int c = 0; c < cols; c++)
		    next_means[k][c] = next_sums[k][c]/rows;

	    if (same (centroids, next_means))
		break;

	    assign(centroids, next_means);
	}
    }
}
