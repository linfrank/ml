package edu.cmu.cs.frank.ml.cluster;

import java.util.Map;

import edu.cmu.cs.frank.ml.classify.Classifier;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.Label;


public class ClusterClassifier implements Classifier{
	
	static final long serialVersionUID=20090312L;

	private Map<Instance,Label> labelMap;

	public ClusterClassifier(Map<Instance,Label> labelMap){
		this.labelMap=labelMap;
	}

	@Override
	public Label classify(Instance instance){
		return labelMap.get(instance);
	}

}
