package edu.cmu.cs.frank.x;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.Parameters;
import edu.cmu.cs.frank.util.ScoreTable;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;


public class ModularityDisambiguator implements Curator{

	static Logger log=Logger.getLogger(ModularityDisambiguator.class);

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

		if(qs==null){
			getAmbiguityScore(graph);
		}

		double qsum=0.0;
		for(double q:qs.scores()){
			qsum+=q;
		}

		log.info("Initial modularity score: "+qsum);

		log.info("Top modularity splitting candidates:");
		for(String name:qs.topKeysAscend(30)){
			log.info(" "+name+" => "+qs.get(name));
		}

		Map<Integer,String> pred=new HashMap<Integer,String>();

		for(String name:qs.keySet()){
			if(qs.get(name)<0){
				// for now just stupidly split everything
				int i=0;
				for(Integer instance:graph.getOutEdges(name)){
					pred.put(instance,CuratorUtil.appendNumber(name,i+1));
					i++;
				}
			}
		}

		return pred;
	}

	ScoreTable<String> qs;

	@Override
	public ScoreTable<String> getAmbiguityScore(DirectedGraph<String,Integer> graph){
		//qs=getModularity(graph,"N:");
		qs=getOutEdgeRatio(graph);
		return qs;
	}

	/**
	 * A different kind of modularity from the usual definition. A more general
	 * way to look at modularity is the expectation of random occurrences in a 
	 * subgraph verses the actual occurrence.
	 * 
	 * @param graph
	 * @param candPref
	 * @return
	 */

//	private static ScoreTable<String> getModularity(DirectedGraph<String,Integer> graph,String candPref){
//
//		ScoreTable<String> q=new ScoreTable<String>(graph.getVertexCount()/2);
//
//		int m=graph.getEdgeCount();
//
//		for(String name:graph.getVertices()){
//			if(name.startsWith(candPref)){
//				int in=0;
//				int all=0;
//				for(String paper:graph.getNeighbors(name)){
//					for(String co:graph.getNeighbors(paper)){
//						in++;
//						all+=graph.getNeighborCount(co);
//					}
//				}
//				q.set(name,((double)in/(2*m))-(double)(all*all)/(4*m*m));
//			}
//		}
//
//		return q;
//
//	}

	private static ScoreTable<String> getOutEdgeRatio(DirectedGraph<String,Integer> ap){

		log.info("Transforming author-paper graph to co-author graph");

		UndirectedGraph<String,Integer> ca=CuratorUtil.toCoAuthorGraph(ap);

		ScoreTable<String> q=new ScoreTable<String>(ca.getVertexCount());

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
				q.set(self,(double)out/all/ca.degree(self));
			}
		}

		return q;

	}

}
