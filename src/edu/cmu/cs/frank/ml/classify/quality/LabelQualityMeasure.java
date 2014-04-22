package edu.cmu.cs.frank.ml.classify.quality;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;

/**
 * Quality measure for label assignment classification problems. Assumes that
 * the predicted and true class labels come from the same LabelSet.
 */

public interface LabelQualityMeasure{
	
	public double measure(List<Label> pred,List<Label> truth);
	
	public double measure(int[][] confusionMatrix);

}
