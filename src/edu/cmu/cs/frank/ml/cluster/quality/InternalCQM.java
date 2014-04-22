package edu.cmu.cs.frank.ml.cluster.quality;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;

/**
 * Internal clustering quality measure. Cluster labels are evaluated based on
 * how well it "fits" its own dataset.
 */

public interface InternalCQM{
	
	public double measure(List<Label> clustering,Dataset dataset);

}
