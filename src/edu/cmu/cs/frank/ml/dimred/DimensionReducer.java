package edu.cmu.cs.frank.ml.dimred;

import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.util.Parametizable;


public interface DimensionReducer extends Parametizable{

	public Dataset reduce(Dataset full);
	
}
