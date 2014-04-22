package edu.cmu.cs.frank.ml.classify.perceptron;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.cmu.cs.frank.ml.classify.BinaryLabel;
import edu.cmu.cs.frank.ml.classify.FeatureSet;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.LabelSet;

/**
 * @author Frank Lin
 */

public class CommitteePerceptronLearner extends PerceptronLearner{

	static final long serialVersionUID=20071115L;

	private int committeeSize;

	SortedSet<Perceptron> committee;
	Perceptron current;

	public CommitteePerceptronLearner(int numEpochs,int committeeSize){
		super(numEpochs);
		this.committeeSize=committeeSize;
	}

	@Override
	protected void initialize(FeatureSet featureSet,LabelSet labelSet){
		committee=new TreeSet<Perceptron>(Collections.reverseOrder());
		current=new Perceptron(featureSet.size());
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
				if(committee.size()<committeeSize){
					committee.add(current);
				}
				else{
					// here we do not add the current one if committee is full; this could change
					if(committee.last().weight<current.weight){
						committee.remove(committee.last());
						committee.add(current);
					}
				}
				current=current.update(instance,truth);
			}
		}
	}
	
	@Override
	protected void finalize(){}

	@Override
	protected Collection<Perceptron> getPerceptrons(){
		return committee;
	}

}
