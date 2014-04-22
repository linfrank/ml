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

public class FinalPerceptronLearner extends PerceptronLearner{

	static final long serialVersionUID=20071115L;
	
	private Perceptron current;
	
	public FinalPerceptronLearner(int numEpochs){
		super(numEpochs);
	}

	@Override
	protected void initialize(FeatureSet featureSet,LabelSet labelSet){
		current=new Perceptron(featureSet.size());
	}
	
	@Override
	protected void runEpoch(List<? extends Instance> instances){
		for(Instance instance:instances){
			// get preceptron prediction score
			double prediction=current.score(instance);
			// determine label
			int truth=((BinaryLabel)instance.getLabel()).getPolarValue();
			// increase weight if prediction is correct, else create new perceptron
			if(prediction*truth>0){
				current.weight+=1.0;
			}
			else{
				current=current.update(instance,truth);
			}
		}
	}
	
	@Override
	protected void finalize(){}

	@Override
	protected Collection<Perceptron> getPerceptrons(){
		Collection<Perceptron> perceptrons=new ArrayList<Perceptron>(1);
		perceptrons.add(current);
		return perceptrons;
	}

}
