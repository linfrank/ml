package edu.cmu.cs.frank.ml.graph.ssl;

import edu.uci.ics.jung.graph.Graph;

public interface VertexClassifier{
	
	public int predict(Graph<LPVertex,LPEdge> g,LPVertex vertex);
	
	public boolean isConfident(Graph<LPVertex,LPEdge> g,LPVertex vertex);
	
	public void signalGraphUpdate();

}
