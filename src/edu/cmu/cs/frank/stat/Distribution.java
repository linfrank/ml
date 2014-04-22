package edu.cmu.cs.frank.stat;

import java.util.Random;

import edu.cmu.cs.frank.util.Parametizable;


public interface Distribution<T> extends Parametizable{
	
	// pmf or pdf
	public double prob(T x);
	
	public T sample(Random random);
	
}
