package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.Collection;

import edu.cmu.cs.frank.ml.graph.WeightedSticky;
import edu.uci.ics.jung.graph.Graph;

public class LPEdge extends WeightedSticky{
	
	public final int trueLabel;
	public int predictedLabel;
	public double weight;
	public boolean alive;
	
	public LPEdge(Object id,double weight,int trueLabel,boolean alive,int predictedLabel){
		super(id,weight);
		this.trueLabel=trueLabel;
		this.alive=alive;
		this.predictedLabel=predictedLabel;
	}
	
	public LPEdge(Object id,double weight,int trueLabel,boolean alive){
		this(id,weight,trueLabel,alive,-1);
	}
	
	public LPEdge(Object id,double weight,int trueLabel){
		this(id,weight,trueLabel,false);
	}
	
	public LPEdge(Object id,int trueLabel){
		this(id,1.0,trueLabel);
	}
	
	public LPEdge(Object id,LPEdge edge){
		super(id,edge.weight);
		this.trueLabel=edge.trueLabel;
		this.predictedLabel=edge.predictedLabel;
		this.alive=edge.alive;
	}
	
	public void reset(){
		predictedLabel=-1;
		weight=1.0;
		alive=false;
	}
	
	public boolean isCorrect(){
		return trueLabel==predictedLabel;
	}
	
	public static double getAccuracy(Collection<LPEdge> edges){
		int numCorrect=0;
		for(LPEdge edge:edges){
			if(edge.isCorrect()){
				numCorrect++;
			}
		}
		return ((double)numCorrect)/edges.size();
	}
	
	public boolean isIntraFaction(Graph<LPVertex,LPEdge> g){
		return g.getSource(this).trueLabel==g.getDest(this).trueLabel;
	}
	
	public boolean isInterFaction(Graph<LPVertex,LPEdge> g){
		return !isIntraFaction(g);
	}
	
	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(super.toString());
		b.append(" [True Label: ").append(trueLabel).append("]");
		b.append(" [Predicted Label: ").append(predictedLabel).append("]");
		b.append(" [Weight: ").append(weight).append("]");
		b.append(" [Alive: ").append(alive).append("]");
		return b.toString();
	}
	
}
