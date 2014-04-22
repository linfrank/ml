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

public class AveragePerceptronLearner extends PerceptronLearner{

	static final long serialVersionUID=20071115L;

	private Perceptron average;
	private Perceptron current;

	public AveragePerceptronLearner(int numEpochs){
		super(numEpochs);
	}

	@Override
	protected void initialize(FeatureSet featureSet,LabelSet labelSet){
		average=new Perceptron(featureSet.size());
		average.weight=1.0;
		current=new Perceptron(featureSet.size());
		current.weight=0.0;
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
				// store current perceptron in the average
				average.add(current);
				// create new perceptron
				current=current.update(instance,truth);
			}
		}
	}
	
	@Override
	protected void finalize(){
		// store the last current perceptron in the average
		average.add(current);
	}

	@Override
	protected Collection<Perceptron> getPerceptrons(){
		Collection<Perceptron> perceptrons=new ArrayList<Perceptron>(1);
		perceptrons.add(average);
		return perceptrons;
	}

}
