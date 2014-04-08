package ai.h2o.algo;

import ai.h2o.algo.KMeans;
import java.io.File;
import water.H2O;

public class KMeansDriver {
    public static void main(String args[]) {
	if (args.length != 2) {
	    System.out.println ("Usage: Kmeans <csv> <nCentroid>");
	    System.exit(0);
	}

	H2O.main(new String[] {});
	H2O.waitForCloudSize(1, 10000);

	new KMeans(new File(args[0])).run (Integer.parseInt(args[1]));

	System.exit(0);
    }
}
