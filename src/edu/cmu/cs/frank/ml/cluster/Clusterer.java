package edu.cmu.cs.frank.ml.cluster;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.util.Parametizable;

public interface Clusterer extends Parametizable{
	
	public List<Label> cluster(Dataset dataset);

}
