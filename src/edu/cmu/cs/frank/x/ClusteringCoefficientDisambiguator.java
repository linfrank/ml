package edu.cmu.cs.frank.x;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.Parameters;
import edu.cmu.cs.frank.util.ScoreTable;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;


public class ClusteringCoefficientDisambiguator implements Curator{

	static Logger log=Logger.getLogger(ClusteringCoefficientDisambiguator.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20111103L;

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

	@Override
	public Map<Integer,String> curate(DirectedGraph<String,Integer> graph){
		Map<Integer,String> pred=new HashMap<Integer,String>();
		return pred;
	}

	@Override
	public ScoreTable<String> getAmbiguityScore(DirectedGraph<String,Integer> ap){

		log.info("Transforming author-paper graph to co-author graph");

		UndirectedGraph<String,Integer> ca=CuratorUtil.toCoAuthorGraph(ap);
		
		log.info("Calculating clustering coefficents");

		ScoreTable<String> scores=new ScoreTable<String>(ca.getVertexCount());

		for(String self:ca.getVertices()){
			int degree=ca.degree(self);
			if(degree>1){
				int in=1; // technically should be 0, but to avoid divide by 0 set to 1
				for(String first:ca.getNeighbors(self)){
					for(String second:ca.getNeighbors(first)){
						if(ca.isNeighbor(self,second)){
							in++;
						}
					}
				}
				// actually the reciprocal of clustering coefficient
				double cc=(double)(degree*degree)/in;
				scores.set(self,cc);
			}
		}
		
		return scores;

	}

}
