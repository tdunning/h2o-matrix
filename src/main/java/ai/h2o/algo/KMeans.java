package ai.h2o.algo;

import ai.h2o.math.H2OMatrix;
import java.io.File;

public class KMeans {
    public static void main(String args[]) {
	H2OMatrix matrix;

	if (args.length != 2) {
	    System.out.println ("Usage: Kmeans <csv> <nCentroid>");
	    System.exit(0);
	}

	System.out.println("File: " + args[0]);
	matrix = new H2OMatrix(new File(args[0]));
    }
}
