package edu.cmu.cs.frank.ml.classify.perceptron;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.cmu.cs.frank.ml.classify.BinaryLabel;
import edu.cmu.cs.frank.ml.classify.FeatureSet;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.LabelSet;

/**
 * @author Frank Lin
 */

public class VotedPerceptronLearner extends PerceptronLearner{
	
	static final long serialVersionUID=20071115L;
	
	private List<Perceptron> perceptrons;
	private Perceptron current;
	
	public VotedPerceptronLearner(int numEpochs){
		super(numEpochs);
	}

	@Override
	protected void initialize(FeatureSet featureSet,LabelSet labelSet){
		perceptrons=new ArrayList<Perceptron>();
		current=new Perceptron(featureSet.size());
		perceptrons.add(current);
	}
	
	@Override
	protected void runEpoch(List<? extends Instance> instances){
		for(Instance instance:instances){
			double prediction=current.score(instance);
			int truth=((BinaryLabel)instance.getLabel()).getPolarValue();
			if(prediction*truth>0){
				current.weight+=1.0;
			}
			else{
				current=current.update(instance,truth);
				perceptrons.add(current);
			}
		}
	}
	
	@Override
	protected void finalize(){}

	@Override
	protected Collection<Perceptron> getPerceptrons(){
		return perceptrons;
	}

}
