package edu.cmu.cs.frank.ml.classify.boosting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.cmu.cs.frank.ml.classify.BinaryLabel;
import edu.cmu.cs.frank.ml.classify.Classifier;
import edu.cmu.cs.frank.ml.classify.Classifiers;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.Learner;

public class DecisionStumpLearner implements Learner{
	
	static final long serialVersionUID=20071113L;

	@Override
	public Classifier learn(Dataset dataset){
		Classifier best=null;
		double leastError=Double.MAX_VALUE;
		// try each feature
		for(int i=0;i<dataset.getFeatureSet().size();i++){
			List<Double> boundaries=getDecisionBoundaries(dataset.getInstances(),i);
			// try each boundary
			for(int j=0;j<boundaries.size();j++){
				DecisionStumpClassifier c;
				double error;
				// try class label POS
				c=new DecisionStumpClassifier(i,boundaries.get(j),BinaryLabel.POS);
				error=Classifiers.getError(c,dataset.getInstances());
				if(error<leastError){
					best=c;
					leastError=error;
				}
				// try class label NEG
				c=new DecisionStumpClassifier(i,boundaries.get(j),BinaryLabel.NEG);
				error=Classifiers.getError(c,dataset.getInstances());
				if(error<leastError){
					best=c;
					leastError=error;
				}
			}
		}
		return best;
	}
	
	private static List<Double> getDecisionBoundaries(List<Instance> instances,int featureId){
		double[] values=new double[instances.size()];
		for(int i=0;i<instances.size();i++){
			values[i]=instances.get(i).getFeatureValue(featureId);
		}
		Arrays.sort(values);
		List<Double> boundaries=new ArrayList<Double>();
		for(int i=0;i<values.length-1;i++){
			if(values[i]<values[i+1]){
				boundaries.add((values[i]+values[i+1])/2);
			}
		}
		return boundaries;
	}

}
