package edu.cmu.cs.frank.ml.classify;

import java.util.Arrays;
import java.util.Map;

import edu.cmu.cs.frank.util.ArrayUtil;

/**
 * A Naive Bayes classifier learner:
 * 
 * Input: binary features
 * Output: multi-class labels
 * 
 * @author Frank Lin
 */

public class NaiveBayesLearner implements Learner{
	
	static final long serialVersionUID=20071113L;
	
	@Override
	public Classifier learn(Dataset dataset){
		
		// create probability tables
		double[] prior=new double[dataset.getLabelSet().size()];
		double[][] likelihood=new double[dataset.getLabelSet().size()][dataset.getFeatureSet().size()];
		
		// do add-one smoothing
		Arrays.fill(prior,1.0);
		for(int i=0;i<likelihood.length;i++){
			Arrays.fill(likelihood[i],1.0);
		}
		// count
		for(Instance instance:dataset){
			int bestId=instance.getLabel().getBestId();
			prior[bestId]++;
			for(Map.Entry<Integer,Double> feature:instance.getFeatures().entrySet()){
				likelihood[bestId][feature.getKey()]+=feature.getValue();
			}
		}
		// normalize
		ArrayUtil.normalize(prior);
		ArrayUtil.normalize(likelihood);
		// return
		return new NaiveBayesClassifier(prior,likelihood,dataset.getLabelSet());
		
	}

}
