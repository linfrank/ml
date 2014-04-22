package edu.cmu.cs.frank.ml.classify;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class Evaluator{
	
	static Logger log=Logger.getLogger(Evaluator.class);

	public static Evaluation evaluate(Classifier classifier,Dataset test){
		List<Label> truth=test.getLabels();
		List<Label> prediction=Classifiers.classify(classifier,test);
		return Evaluation.calculate(test.getLabelSet(),truth,prediction);
	}

	public static Evaluation evaluate(Learner learner,Dataset dataset,int numFolds,double trainFraction){

		List<Label> truth=new ArrayList<Label>();
		List<Label> prediction=new ArrayList<Label>();

		List<Dataset> folds=dataset.split(numFolds);
		for(int i=0;i<folds.size();i++){
			log.info("Fold "+i);
			Dataset train=new Dataset(dataset.getLabelSet(),dataset.getFeatureSet());
			for(int j=0;j<folds.size();j++){
				if(i!=j){
					train.add(folds.get(j).getInstances());
				}
			}

			if(trainFraction<1.0){
				int numTrain=Math.min((int)(trainFraction*dataset.size()),train.size());
				log.info(numTrain);
				train=train.subset(numTrain);
			}

			Dataset test=folds.get(i);
			
			log.info("Training data size: "+train.size());
			log.info("Test data size: "+test.size());

			Classifier classifier=learner.learn(train);

			truth.addAll(test.getLabels());
			prediction.addAll(Classifiers.classify(classifier,test));
		}

		return Evaluation.calculate(dataset.getLabelSet(),truth,prediction);
	}

	public static Evaluation evaluate(Learner learner,Dataset dataset,int numFolds){
		return evaluate(learner,dataset,numFolds,1.0);
	}

}
