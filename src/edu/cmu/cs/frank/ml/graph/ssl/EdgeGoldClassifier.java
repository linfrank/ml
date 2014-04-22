package edu.cmu.cs.frank.ml.graph.ssl;

import edu.uci.ics.jung.graph.Graph;

public class EdgeGoldClassifier implements EdgeClassifier{

	@Override
	public int predict(Graph<LPVertex,LPEdge> g,LPEdge e){
		return e.trueLabel;
	}

	@Override
	public boolean isConfident(Graph<LPVertex,LPEdge> g,LPEdge e){
		return true;
	}
	
	@Override
	public void signalGraphUpdate(){}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}

}
