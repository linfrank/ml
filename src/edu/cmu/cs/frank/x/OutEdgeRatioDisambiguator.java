package edu.cmu.cs.frank.x;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.Parameters;
import edu.cmu.cs.frank.util.ScoreTable;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;


public class OutEdgeRatioDisambiguator implements Curator{

	static Logger log=Logger.getLogger(OutEdgeRatioDisambiguator.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20111103L;
		
		boolean normalized=false;

	}

	@Override
	public void setParams(Parameters p){
		this.p=(Params)p;
	}

	@Override
	public Parameters getParams(){
		return p;
	}

	private Params p=new Params();
	
	public OutEdgeRatioDisambiguator(boolean normalized){
		p.normalized=normalized;
	}

	@Override
	public Map<Integer,String> curate(DirectedGraph<String,Integer> graph){
		Map<Integer,String> pred=new HashMap<Integer,String>();
		return pred;
	}

	@Override
	public ScoreTable<String> getAmbiguityScore(DirectedGraph<String,Integer> ap){

		log.info("Transforming author-paper graph to co-author graph");

		UndirectedGraph<String,Integer> ca=CuratorUtil.toCoAuthorGraph(ap);
		
		log.info("Counting out-edge ratio");

		ScoreTable<String> scores=new ScoreTable<String>(ca.getVertexCount());

		for(String self:ca.getVertices()){
			if(ca.degree(self)>1){
				int out=0;
				int all=0;
				for(String first:ca.getNeighbors(self)){
					for(String second:ca.getNeighbors(first)){
						all++;
						if(!ca.isNeighbor(self,second)){
							out++;
						}
					}
				}
				double ratio=(double)out/all;
				if(p.normalized){
					ratio=ratio/ca.degree(self);
				}
				scores.set(self,ratio);
			}
		}
		
		return scores;

	}

}
