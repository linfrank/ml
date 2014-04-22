package edu.cmu.cs.frank.stat;

import java.util.Random;

import edu.cmu.cs.frank.util.Parametizable;

/**
 * A random process that generates states of type T
 */

public interface Process<T> extends Parametizable{
	
	public void initProcess(Random random);
	
	public T generateNext(Random random);
	
}
