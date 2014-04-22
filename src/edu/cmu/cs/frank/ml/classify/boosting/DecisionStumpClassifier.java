package edu.cmu.cs.frank.ml.classify.boosting;

import edu.cmu.cs.frank.ml.classify.BinaryLabel;
import edu.cmu.cs.frank.ml.classify.Classifier;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.Label;

public class DecisionStumpClassifier implements Classifier{
	
	static final long serialVersionUID=20071113L;
	
	private int featureId;
	private double threshold;
	private BinaryLabel label;
	
	public DecisionStumpClassifier(int featureId,double threshold,BinaryLabel label){
		this.featureId=featureId;
		this.threshold=threshold;
		this.label=label;
	}
	
	@Override
	public Label classify(Instance instance){
		if(instance.getFeatureValue(featureId)<threshold){
			return BinaryLabel.not(label);
		}
		else{
			return label;
		}
	}
	
	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(this.getClass().getSimpleName()).append(" [feature=").append(featureId).append(", threshold=").append(threshold).append(", class=").append(label).append("]");
		return b.toString();
	}

}
