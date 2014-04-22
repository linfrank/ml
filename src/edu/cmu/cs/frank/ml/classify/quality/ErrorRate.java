package edu.cmu.cs.frank.ml.classify.quality;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;

/**
 * Error rate; eqauls to 1 - accuracy.
 * (# incorrect labels) / (# total labels)
 */

public class ErrorRate implements LabelQualityMeasure{
	
	private Accuracy acc=new Accuracy();
	
	@Override
	public double measure(List<Label> pred,List<Label> truth){
		return measure(LQMUtil.makeConfusionMatrix(pred,truth));
	}
	
	@Override
	public double measure(int[][] cm){
		return 1.0-acc.measure(cm);
	}

}
