package edu.cmu.cs.frank.x;

import java.util.Map;

import edu.cmu.cs.frank.util.Parametizable;
import edu.cmu.cs.frank.util.ScoreTable;
import edu.uci.ics.jung.graph.DirectedGraph;


public interface Curator extends Parametizable{
	
	public Map<Integer,String> curate(DirectedGraph<String,Integer> graph);
	
	public ScoreTable<String> getAmbiguityScore(DirectedGraph<String,Integer> graph);

}
