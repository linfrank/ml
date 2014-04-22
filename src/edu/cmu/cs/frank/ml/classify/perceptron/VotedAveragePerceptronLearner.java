package edu.cmu.cs.frank.ml.classify.perceptron;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.BinaryLabel;
import edu.cmu.cs.frank.ml.classify.FeatureSet;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.LabelSet;

/**
 * @author Frank Lin
 */

public class VotedAveragePerceptronLearner extends PerceptronLearner{
	
	static final long serialVersionUID=20071115L;
	
	protected static Logger logger=Logger.getLogger(VotedAveragePerceptronLearner.class);
	
	private int maxPerceptrons;
	
	SortedSet<Perceptron> perceptrons;
	Perceptron current;
	
	public VotedAveragePerceptronLearner(int numEpochs,int maxPerceptrons){
		super(numEpochs);
		this.maxPerceptrons=maxPerceptrons;
	}

	@Override
	protected void initialize(FeatureSet featureSet,LabelSet labelSet){
		perceptrons=new TreeSet<Perceptron>(Collections.reverseOrder());
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
				perceptrons.add(current);
				if(perceptrons.size()>maxPerceptrons){
					// create a list of perceptrons to be averaged
					List<Perceptron> averaging=new ArrayList<Perceptron>();
					// find the smallest weight; all perceptrons with this weight will be averaged
					double smallestWeight=perceptrons.last().weight;
					// add all the smallest weight to the averaging list and remove them from set
					while(perceptrons.last().weight==smallestWeight){
						averaging.add(perceptrons.last());
						perceptrons.remove(perceptrons.last());
					}
					// we should at least average two perceptrons
					if(averaging.size()<2&&perceptrons.size()>0){
						averaging.add(perceptrons.last());
						perceptrons.remove(perceptrons.last());
					}
					// average the perceptrons and put it to the test
					Perceptron averaged=Perceptron.average(current.vector.length,averaging,smallestWeight);
					current=averaged;
				}
				else{
					current=current.update(instance,truth);
				}
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
