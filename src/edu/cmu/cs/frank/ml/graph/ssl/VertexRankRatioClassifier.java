package edu.cmu.cs.frank.ml.graph.ssl;

import edu.cmu.cs.frank.util.ArrayUtil;
import edu.uci.ics.jung.graph.Graph;

public class VertexRankRatioClassifier extends AbstractVertexClassifier{
	
	private double minConfidenceRatio;

	public VertexRankRatioClassifier(double minConfidenceRatio){
		super();
		this.minConfidenceRatio=minConfidenceRatio;
	}
	
	public double getMinConfidenceRatio(){
		return minConfidenceRatio;
	}
	
	public void setMinConfidenceRatio(double minConfidenceRatio){
		this.minConfidenceRatio=minConfidenceRatio;
	}
	
	@Override
	protected void makePrediction(Graph<LPVertex,LPEdge> g,LPVertex v){
		double[] ranks=v.scores;
		int label=ArrayUtil.maxIndex(ranks);
		double ratio=ranks[label]/(ArrayUtil.sum(ranks)/ranks.length);
		labels.put(v,label);
		confidences.put(v,ratio>=minConfidenceRatio);
	}
	
	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(super.toString()).append(":");
		b.append(" Min Confidence Ratio=").append(minConfidenceRatio);
		return b.toString();
	}

}
