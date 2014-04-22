package edu.cmu.cs.frank.ml.cluster.quality;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;

/**
 * External clustering quality measure. External means the cluster labels are
 * compared to some ground truth.
 * 
 * Does NOT assume that the clustering and true labels come from the same 
 * LabelSet.
 */

public interface ExternalCQM{
	
	public double measure(List<Label> clustering,List<Label> truth);

}
