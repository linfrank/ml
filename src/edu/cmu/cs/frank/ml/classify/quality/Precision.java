package edu.cmu.cs.frank.ml.classify.quality;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.util.ArrayUtil;

/**
 * Macro-averaged precision:
 * (# correct labels per class) / (# predicted labels per class)
 */

public class Precision implements LabelQualityMeasure{
	
	@Override
	public double measure(List<Label> pred,List<Label> truth){
		return measure(LQMUtil.makeConfusionMatrix(pred,truth));
	}
	
	@Override
	public double measure(int[][] cm){
		double[] perClass=new double[cm.length];
		for(int i=0;i<cm.length;i++){
			int total=ArrayUtil.sum(cm[i]);
			if(total>0){
				perClass[i]=(double)cm[i][i]/total;
			}
		}
		return ArrayUtil.average(perClass);
	}

}
