package edu.cmu.cs.frank.ml.classify.quality;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.util.ArrayUtil;

/**
 * Simple accuracy; same as 1 - error rate.
 * (# correct labels) / (# total labels)
 */

public class Accuracy implements LabelQualityMeasure{
	
	@Override
	public double measure(List<Label> pred,List<Label> truth){
		return measure(LQMUtil.makeConfusionMatrix(pred,truth));
	}
	
	@Override
	public double measure(int[][] cm){
		int correct=0;
		int total=0;
		for(int i=0;i<cm.length;i++){
			correct+=cm[i][i];
			total+=ArrayUtil.sum(cm[i]);
		}
		return total>0?((double)correct/total):0.0;
	}

}
