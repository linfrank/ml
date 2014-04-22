package edu.cmu.cs.frank.ml.classify.boosting;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.BinaryLabel;
import edu.cmu.cs.frank.ml.classify.Classifier;
import edu.cmu.cs.frank.ml.classify.Classifiers;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.Learner;

public class AdaBoostLearner implements Learner{
	
	static final long serialVersionUID=20071113L;

	private Learner baseLearner;
	private int rounds;
	
	private static Logger log=Logger.getLogger(AdaBoostLearner.class);

	public AdaBoostLearner(Learner baseLearner,int rounds){
		this.baseLearner=baseLearner;
		this.rounds=rounds;
	}
	
	public AdaBoostLearner(Learner baseLearner){
		this(baseLearner,1);
	}
	
	public Learner getBaseLearner(){
		return baseLearner;
	}

	public void setBaseLearner(Learner baseLearner){
		this.baseLearner=baseLearner;
	}

	public int getRounds(){
		return rounds;
	}

	public void setRounds(int rounds){
		this.rounds=rounds;
	}

	public List<Classifier> learnAllRounds(Dataset dataset){
		
		List<Instance> instances=dataset.getInstances();
		
		//Initialize list of final classifiers, one per round
		List<Classifier> finals=new LinkedList<Classifier>();
		
		//Initialize model 
		List<Double> alphas=new LinkedList<Double>();
		List<Classifier> baseClassifiers=new LinkedList<Classifier>();

		//Initialize example distribution
		List<Instance> weighted=new ArrayList<Instance>();
		for(Instance instance:instances){
			Instance copy=instance.shallowCopy();
			copy.setWeight(1.0/instances.size());
			weighted.add(copy);
		}
		
		log.info("Boosting...");

		//Run rounds
		for(int t=0;t<rounds;t++){
			log.info("Round: "+t);
			//Get base classifier
			Classifier classifier=baseLearner.learn(new Dataset(weighted));
			baseClassifiers.add(classifier);
			//Get error
			double error=Classifiers.getError(classifier,weighted);
			//Get alpha
			double alpha=0.5*Math.log((1.0-error)/error);
			alphas.add(alpha);
			//Initialize normalizer
			double z=0;
			//Update weights
			for(Instance instance:weighted){
				double newWeight=instance.getWeight()*Math.exp(-1.0*alpha*((BinaryLabel)instance.getLabel()).getPolarValue()*((BinaryLabel)classifier.classify(instance)).getPolarValue());
				instance.setWeight(newWeight);
				z+=newWeight;
			}
			//Normalize weights
			for(Instance instance:weighted){
				instance.setWeight(instance.getWeight()/z);
			}
			//Create the final classifier up to this round
			AdaBoostClassifier finalClassifier=new AdaBoostClassifier(new LinkedList<Double>(alphas),new LinkedList<Classifier>(baseClassifiers));
			finals.add(finalClassifier);
			log.info(new StringBuilder()
			.append("Round details:").append("\n")
			.append("base classifier: ").append(classifier).append("\n")
			.append("error: ").append(error).append("\n")
			.append("alpha: ").append(alpha).append("\n")
			.append("z: ").append(z).append("\n")
			.append("examples: ").append(weighted)
			.toString()
			);
		}
		
		log.info("Finished boosting.");
		
		//Return final classifiers
		return finals;
	}

	@Override
	public Classifier learn(Dataset dataset){
		return learnAllRounds(dataset.copy()).get(rounds-1);
	}
	
	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append("Base Learner: "+baseLearner).append("\n");
		b.append("Rounds: "+rounds).append("\n");
		return b.toString();
	}

}
