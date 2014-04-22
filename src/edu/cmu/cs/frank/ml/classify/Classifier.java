package edu.cmu.cs.frank.ml.classify;

import java.io.Serializable;


public interface Classifier extends Serializable{
	
	public Label classify(Instance instance);
	
}
