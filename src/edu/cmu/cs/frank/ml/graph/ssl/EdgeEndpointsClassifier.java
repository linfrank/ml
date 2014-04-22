package edu.cmu.cs.frank.ml.graph.ssl;

import edu.uci.ics.jung.graph.Graph;

public class EdgeEndpointsClassifier implements EdgeClassifier{

	@Override
	public int predict(Graph<LPVertex,LPEdge> g,LPEdge e){
		if(g.getDest(e).confident){
			return g.getDest(e).predictedLabel;
		}
		else if(g.getSource(e).confident){
			return g.getSource(e).predictedLabel;
		}
		else{
			return g.getDest(e).predictedLabel;
		}
	}

	@Override
	public boolean isConfident(Graph<LPVertex,LPEdge> g,LPEdge e){
		return g.getDest(e).confident||g.getSource(e).confident;
	}
	
	@Override
	public void signalGraphUpdate(){}
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}

}
