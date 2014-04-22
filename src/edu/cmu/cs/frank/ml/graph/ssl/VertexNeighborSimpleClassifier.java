package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.Collection;

import edu.cmu.cs.frank.util.ArrayUtil;
import edu.uci.ics.jung.graph.Graph;

public class VertexNeighborSimpleClassifier implements VertexClassifier{
	
	double neighborConfidence;
	
	public VertexNeighborSimpleClassifier(double neighborConfidence){
		this.neighborConfidence=neighborConfidence;
	}

	@Override
	public boolean isConfident(Graph<LPVertex,LPEdge> g,LPVertex v){
		return true;
	}

	@Override
	public int predict(Graph<LPVertex,LPEdge> g,LPVertex v){
		double[] scores=new double[v.getDimensions()];
		Collection<LPVertex> neighborhood=g.getNeighbors(v);
		for(LPVertex neighbor:neighborhood){
			scores=ArrayUtil.add(scores,neighbor.scores);
		}
		scores=ArrayUtil.multiply(scores,neighborConfidence);
		scores=ArrayUtil.add(scores,v.scores);
		return ArrayUtil.maxIndex(scores);
	}

	@Override
	public void signalGraphUpdate(){
		// do nothing
	}

	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}

}
