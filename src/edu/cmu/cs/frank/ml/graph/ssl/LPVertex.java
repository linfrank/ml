package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.frank.ml.graph.StickyId;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.uci.ics.jung.graph.Graph;

public class LPVertex extends StickyId implements Comparable<LPVertex>{
	
	public final int trueLabel;
	public int predictedLabel;
	public double[] scores;
	public boolean confident;
	public boolean seed;

	public LPVertex(Object id,int trueLabel,double[] scores){
		super(id);
		this.trueLabel=trueLabel;
		this.scores=scores;
		predictedLabel=-1;
		confident=false;
		seed=false;
	}
	
	public LPVertex(Object id,int trueLabel,int dim){
		this(id,trueLabel,new double[dim]);
		Arrays.fill(scores,1.0);
	}
	
	public LPVertex(Object id,LPVertex other){
		super(id);
		this.trueLabel=other.trueLabel;
		this.predictedLabel=other.predictedLabel;
		this.confident=other.confident;
		this.scores=ArrayUtil.copy(other.scores);
		this.seed=other.seed;
	}
	
	public void reset(){
		predictedLabel=-1;
		scores=new double[scores.length];
		Arrays.fill(scores,1.0);
		confident=false;
		seed=false;
	}
	
	public void makeSeed(){
		predictedLabel=trueLabel;
		confident=true;
		seed=true;
	}
	
	public double getTotalScore(){
		return ArrayUtil.sum(scores);
	}
	
	public int getDimensions(){
		return scores.length;
	}
	
	public int degree(Graph<LPVertex,LPEdge> g,int faction){
		int degree=0;
		for(LPVertex neighbor:g.getNeighbors(this)){
			if(neighbor.trueLabel==this.trueLabel){
				degree++;
			}
		}
		return degree;
	}
	
	public int predictedInDegree(Graph<LPVertex,LPEdge> g,int faction){
		int numLabelDegree=0;
		for(LPEdge edge:g.getInEdges(this)){
			if(edge.predictedLabel==faction){
				numLabelDegree++;
			}
		}
		return numLabelDegree;
	}
	
	public int predictedOutDegree(Graph<LPVertex,LPEdge> g,int faction){
		int numLabelDegree=0;
		for(LPEdge edge:g.getOutEdges(this)){
			if(edge.predictedLabel==faction){
				numLabelDegree++;
			}
		}
		return numLabelDegree;
	}
	
	public Set<LPEdge> getLiveInEdges(Graph<LPVertex,LPEdge> g){
		Set<LPEdge> live=new HashSet<LPEdge>();
		for(LPEdge edge:g.getInEdges(this)){
			if(edge.alive){
				live.add(edge);
			}
		}
		return live;
	}
	
	public Set<LPEdge> getLiveOutEdges(Graph<LPVertex,LPEdge> g){
		Set<LPEdge> live=new HashSet<LPEdge>();
		for(LPEdge edge:g.getInEdges(this)){
			if(edge.alive){
				live.add(edge);
			}
		}
		return live;
	}
	
	public Set<LPEdge> getLiveIncidentEdges(Graph<LPVertex,LPEdge> g){
		Set<LPEdge> live=new HashSet<LPEdge>();
		for(LPEdge edge:g.getIncidentEdges(this)){
			if(edge.alive){
				live.add(edge);
			}
		}
		return live;
	}
	
	public boolean isAlive(Graph<LPVertex,LPEdge> g){
		return getLiveIncidentEdges(g).size()>0;
	}
	
	public boolean isCorrect(){
		return trueLabel==predictedLabel;
	}
	
	public boolean isInBorderVertex(Graph<LPVertex,LPEdge> g){
		for(LPVertex pred:g.getPredecessors(this)){
			if(trueLabel!=pred.trueLabel){
				return true;
			}
		}
		return false;
	}
	
	public boolean isOutBorderVertex(Graph<LPVertex,LPEdge> g){
		for(LPVertex succ:g.getSuccessors(this)){
			if(trueLabel!=succ.trueLabel){
				return true;
			}
		}
		return false;
	}
	
	public boolean isBorderVertex(Graph<LPVertex,LPEdge> g){
		return isInBorderVertex(g)||isOutBorderVertex(g);
	}
	
	@Override
	public int compareTo(LPVertex other){
		double diff=ArrayUtil.sum(scores)-ArrayUtil.sum(other.scores);
		if(diff>0){
			return 1;
		}
		else if(diff<0){
			return -1;
		}
		else{
			return 0;
		}
	}
	
	public static double getAccuracy(Collection<LPVertex> vertices){
		int numCorrect=0;
		for(LPVertex vertex:vertices){
			if(vertex.isCorrect()){
				numCorrect++;
			}
		}
		return ((double)numCorrect)/vertices.size();
	}
	
	public static Comparator<LPVertex> getFactionComparator(int faction){
		final int f=faction;
		return new Comparator<LPVertex>(){
			@Override
			public int compare(LPVertex v1,LPVertex v2){
				double diff=v1.scores[f]-v2.scores[f];
				if(diff>0){
					return 1;
				}
				else if(diff<0){
					return -1;
				}
				else{
					return 0;
				}
			}
		};
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(super.toString());
		b.append(" [True Label: ").append(trueLabel).append("]");
		b.append(" [Predicted Label: ").append(predictedLabel).append("]");
		b.append(" [Prediction Confident: ").append(confident).append("]");
		b.append(" [Scores: ").append(Arrays.toString(scores)).append("]");
		b.append(" [Is Seed: ").append(seed).append("]");
		return b.toString();
	}

}
