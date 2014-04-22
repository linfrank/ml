package edu.cmu.cs.frank.ml.classify.quality;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;

/**
 * Macro-averaged F-score
 * (1+beta^2)*(precision*recall)/(beta^2*precision+recall)
 * 
 * beta > 1 - care more about recall
 * beta < 1 - care more about precision
 */

public class FScore implements LabelQualityMeasure{
	
	private Precision precision=new Precision();
	private Recall recall=new Recall();
	private double beta;
	
	public FScore(double beta){
		this.beta=beta;
	}
	
	@Override
	public double measure(List<Label> pred,List<Label> truth){
		return measure(LQMUtil.makeConfusionMatrix(pred,truth));
	}
	
	@Override
	public double measure(int[][] cm){
		double p=precision.measure(cm);
		double r=recall.measure(cm);
		if(p+r==0){
			return 0.0;
		}
		else{
			return (1.0+beta*beta)*(p*r)/(beta*beta*p+r); 
		}
	}

}
