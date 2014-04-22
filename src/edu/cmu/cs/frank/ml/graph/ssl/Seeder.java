package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.Set;

public interface Seeder{
	
	public Set<LPVertex> seed(LPGraph graph);
	
}
