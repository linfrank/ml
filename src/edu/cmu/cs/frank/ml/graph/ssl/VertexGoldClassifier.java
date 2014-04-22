package edu.cmu.cs.frank.ml.graph.ssl;

import edu.uci.ics.jung.graph.Graph;

public class VertexGoldClassifier implements VertexClassifier{

	@Override
	public int predict(Graph<LPVertex,LPEdge> g,LPVertex e){
		return e.trueLabel;
	}

	@Override
	public boolean isConfident(Graph<LPVertex,LPEdge> g,LPVertex e){
		return true;
	}
	
	@Override
	public void signalGraphUpdate(){}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}

}
