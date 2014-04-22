package edu.cmu.cs.frank.ml.cluster.sim;

import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.util.Parametizable;

public interface Similarity extends Parametizable{
	
	public double measure(Instance a,Instance b);
	
}
