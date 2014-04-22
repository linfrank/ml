package edu.cmu.cs.frank.ml.classify;

import java.util.Arrays;


/**
 * A Naive Bayes classifier:
 * 
 * Input: binary features
 * Output: multi-class labels
 * 
 * @author Frank Lin
 */

public class NaiveBayesClassifier implements Classifier{

	static final long serialVersionUID=20071113L;

	private double[] prior;
	private double[][] likelihood;
	private LabelSet labelSet;

	public NaiveBayesClassifier(double[] prior,double[][] likelihood,LabelSet labelSet){
		this.prior=prior;
		this.likelihood=likelihood;
		this.labelSet=labelSet;
	}

	@Override
	public Label classify(Instance instance){
		// calculate posterior distributions, using log prob to avoid underflow
		double[] posterior=new double[prior.length];
		Arrays.fill(posterior,0.0);
		for(int i=0;i<prior.length;i++){
			posterior[i]=Math.log(prior[i]);
		}
		for(int i=0;i<likelihood.length;i++){
			for(int j=0;j<likelihood[i].length;j++){
				if(instance.getFeatureValue(j)>0){
					posterior[i]+=Math.log(likelihood[i][j]);
				}
			}
		}
		//System.out.println("Predict: "+ArrayUtil.maxIndex(posterior)+" from "+Arrays.toString(posterior));
		return labelSet.newLabel(posterior);
	}

}
