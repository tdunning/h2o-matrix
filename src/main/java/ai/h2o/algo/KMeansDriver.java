package ai.h2o.algo;

import ai.h2o.math.H2OMatrix;
import java.io.File;
import java.io.IOException;

import water.H2O;

public class KMeansDriver {
  public static void main(String args[]) throws IOException {
    if (args.length != 2) {
	    System.out.println ("Usage: Kmeans <csv> <nCentroid>");
	    System.exit(0);
    }

    H2O.main(new String[] {});
    H2O.waitForCloudSize(1, 10000);

    new KMeans(new H2OMatrix(new File(args[0]))).run (Integer.parseInt(args[1]));

    System.exit(0);
  }
}
