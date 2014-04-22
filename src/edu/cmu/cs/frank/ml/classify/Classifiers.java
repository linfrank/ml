package edu.cmu.cs.frank.ml.classify;

import java.util.ArrayList;
import java.util.List;

public class Classifiers{
	
	public static List<Label> classify(Classifier classifier,List<Instance> instances){
		List<Label> labels=new ArrayList<Label>();
		for(Instance instance:instances){
			labels.add(classifier.classify(instance));
		}
		return labels;
	}
	
	public static List<Label> classify(Classifier classifier,Dataset dataset){
		return classify(classifier,dataset.getInstances());
	}
	
	public static double getError(Classifier classifier,List<Instance> instances){
		double error=0;
		for(Instance instance:instances){
			if(classifier.classify(instance).getBestId()!=instance.label.getBestId()){
				error+=instance.weight;
			}
		}
		return error;
	}
	
	public static double getError(Classifier classifier,Dataset dataset){
		return getError(classifier,dataset.getInstances());
	}
	
	public static double getNormalizedError(Classifier classifier,List<Instance> instances){
		double totalWeight=0.0;
		for(Instance instance:instances){
			totalWeight+=instance.weight;
		}
		return getError(classifier,instances)/totalWeight;
	}
	
	public static double getNormalizedError(Classifier classifier,Dataset dataset){
		return getNormalizedError(classifier,dataset.getInstances());
	}

}
