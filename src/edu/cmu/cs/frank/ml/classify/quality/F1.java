package edu.cmu.cs.frank.ml.classify.quality;


/**
 * Macro-averaged F1-score
 * 2.0*(precision*recall)/(precision+recall)
 */

public class F1 extends FScore{
	
	public F1(){
		super(1.0);
	}


}
