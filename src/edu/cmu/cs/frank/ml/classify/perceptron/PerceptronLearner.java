package edu.cmu.cs.frank.ml.classify.perceptron;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.Classifier;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.FeatureSet;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.LabelSet;
import edu.cmu.cs.frank.ml.classify.Learner;

/**
 * @author Frank Lin
 */

public abstract class PerceptronLearner implements Learner{
	
	static final long serialVersionUID=20081016L;
	
	protected static Logger logger=Logger.getLogger(PerceptronLearner.class);
	
	protected int numEpochs;
	protected boolean shuffle; 
	
	public PerceptronLearner(int numEpochs,boolean shuffle){
		this.numEpochs=numEpochs;
		this.shuffle=shuffle;
	}
	
	public PerceptronLearner(int numEpochs){
		this(numEpochs,false);
	}
	
	public int getNumEpochs(){
		return numEpochs;
	}
	
	public void setNumEpochs(int numEpochs){
		this.numEpochs=numEpochs;
	}
	
	public boolean isShuffle(){
		return shuffle;
	}
	
	public void setShuffle(boolean shuffle){
		this.shuffle=shuffle;
	}

	@Override
	public Classifier learn(Dataset dataset){
		initialize(dataset.getFeatureSet(),dataset.getLabelSet());
		for(int i=0;i<numEpochs;i++){
			logger.debug("Epoch "+i+" shuffle="+shuffle);
			if(shuffle){
				Collections.shuffle(dataset.getInstances());
			}
			runEpoch(dataset.getInstances());
			logger.debug("End of epoch "+i+" perceptrons="+getPerceptrons().size());
		}
		finalize();
		return new PerceptronClassifier(getPerceptrons());
	}
	
	protected abstract void initialize(FeatureSet featureSet,LabelSet labelSet);
	
	protected abstract void runEpoch(List<? extends Instance> instances);
	
	@Override
	protected abstract void finalize();
	
	protected abstract Collection<Perceptron> getPerceptrons();

}
