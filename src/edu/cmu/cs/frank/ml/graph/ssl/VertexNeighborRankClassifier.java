package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.Collection;

import edu.cmu.cs.frank.util.ArrayUtil;
import edu.uci.ics.jung.graph.Graph;

public class VertexNeighborRankClassifier extends AbstractVertexClassifier{
	
	private double neighborConfidence;
	private boolean successorsOnly;
	private double threshold;
	
	public VertexNeighborRankClassifier(double neighborConfidence,boolean successorsOnly,double threshold){
		this.neighborConfidence=neighborConfidence;
		this.successorsOnly=successorsOnly;
		this.threshold=threshold;
	}
	
	public VertexNeighborRankClassifier(double neighborConfidence,boolean successorsOnly){
		this(neighborConfidence,successorsOnly,1.0);
	}
	
	public VertexNeighborRankClassifier(double neighborConfidence,double threshold){
		this(neighborConfidence,true,threshold);
	}
	
	public VertexNeighborRankClassifier(double neighborConfidence){
		this(neighborConfidence,true);
	}
	
	public VertexNeighborRankClassifier(boolean successorsOnly){
		this(1.0,successorsOnly);
	}
	
	public VertexNeighborRankClassifier(){
		this(1.0);
	}
	
	protected double[] getScores(Graph<LPVertex,LPEdge> g,LPVertex v){
		double[] scores=new double[v.getDimensions()];
		Collection<LPVertex> neighborhood;
		if(successorsOnly){
			neighborhood=g.getSuccessors(v);
		}
		else{
			neighborhood=g.getNeighbors(v);
		}
		for(LPVertex neighbor:neighborhood){
			scores=ArrayUtil.multiply(ArrayUtil.add(scores,neighbor.scores),neighborConfidence);
		}
		scores=ArrayUtil.add(scores,v.scores);
		return scores;
	}
	
	@Override
	protected void makePrediction(Graph<LPVertex,LPEdge> g,LPVertex v){
		double[] scores=getScores(g,v);
		labels.put(v,ArrayUtil.maxIndex(scores));
		double neighbors;
		if(successorsOnly){
			neighbors=g.getSuccessors(v).size();
		}
		else{
			neighbors=g.getNeighbors(v).size();
		}
		double weightedNeighbors=neighbors*neighborConfidence;
		double uniform=1.0/g.getVertexCount();
		boolean confident=ArrayUtil.max(scores)/(1.0+weightedNeighbors)>uniform*threshold;
		confidences.put(v,confident);
	}
	
	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(super.toString()).append(":");
		b.append(" Neighbor Confidence=").append(neighborConfidence);
		b.append(" Succesors Only=").append(successorsOnly);
		return b.toString();
	}

}
