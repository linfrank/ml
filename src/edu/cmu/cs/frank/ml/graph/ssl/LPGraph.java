package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import edu.cmu.cs.frank.ml.classify.LabelSet;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * @author Frank Lin
 */

public class LPGraph extends DirectedSparseGraph<LPVertex,LPEdge>{
	
	static final long serialVersionUID=20111015L;
	
	protected int dimensions;
	protected LabelSet labelSet;
	
	public LPGraph(int dimensions,LabelSet labelSet){
		this.dimensions=dimensions;
		this.labelSet=labelSet;
	}
	
	public LPGraph(int dimensions){
		this(dimensions,new LabelSet());
	}
	
	public void reset(){
		for(LPVertex v:getVertices()){
			v.reset();
		}
		for(LPEdge e:getEdges()){
			e.reset();
		}
	}
	
	public int getDimensions(){
		return dimensions;
	}
	
	public LabelSet getLabelSet(){
		return labelSet;
	}
	
	public void setLabelSet(LabelSet labelSet){
		this.labelSet=labelSet;
	}
	
	public List<LPVertex> getRankedFaction(int faction){
		List<LPVertex> ranked=new ArrayList<LPVertex>(getVertices());
		Collections.sort(ranked,LPVertex.getFactionComparator(faction));
		Collections.reverse(ranked);
		return ranked;
	}
	
	public double getVertexAccuracy(){
		double numCorrect=0;
		for(LPVertex v:getVertices()){
			if(v.isCorrect()){
				numCorrect++;
			}
		}
		return numCorrect/vertices.size();
	}

	public double getEdgeAccuracy(){
		double numCorrect=0;
		for(LPEdge edge:getEdges()){
			if(edge.isCorrect()){
				numCorrect++;
			}
		}
		return numCorrect/edges.size();
	}

	public List<List<LPVertex>> getRankings(Comparator<LPVertex> comparator){
		List<List<LPVertex>> rankings=new ArrayList<List<LPVertex>>(dimensions);
		for(int i=0;i<dimensions;i++){
			rankings.add(new ArrayList<LPVertex>());
		}
		for(LPVertex v:getVertices()){
			rankings.get(ArrayUtil.maxIndex(v.scores)).add(v);
		}
		for(int i=0;i<dimensions;i++){
			Collections.sort(rankings.get(i));
		}
		return rankings;
		
	}

	public List<List<LPVertex>> getRankings(){
		Comparator<LPVertex> c=new Comparator<LPVertex>(){
			@Override
			public int compare(LPVertex v1,LPVertex v2){
				double ratio1=ArrayUtil.max(v1.scores)/ArrayUtil.sum(v1.scores);
				double ratio2=ArrayUtil.max(v2.scores)/ArrayUtil.sum(v2.scores);
				if(ratio1>ratio2){
					return -1;
				}
				else if(ratio1<ratio2){
					return 1;
				}
				else{
					return 0;
				}
			}
		};
		return getRankings(c);
	}
	
}
