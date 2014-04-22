package edu.cmu.cs.frank.ml.classify;

import java.io.Serializable;


public interface Learner extends Serializable{
	
	public Classifier learn(Dataset dataset);
	
}
