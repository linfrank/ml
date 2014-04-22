package edu.cmu.cs.frank.ml.graph.ssl;

import edu.uci.ics.jung.graph.Graph;

public interface EdgeClassifier{
	
	public int predict(Graph<LPVertex,LPEdge> g,LPEdge e);
	
	public boolean isConfident(Graph<LPVertex,LPEdge> g,LPEdge e);
	
	public void signalGraphUpdate();

}
