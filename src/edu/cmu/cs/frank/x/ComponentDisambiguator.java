package edu.cmu.cs.frank.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.Parameters;
import edu.cmu.cs.frank.util.ScoreTable;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;


public class ComponentDisambiguator implements Curator{

	static Logger log=Logger.getLogger(ComponentDisambiguator.class);

	public static class Params extends Parameters{
		
		static final long serialVersionUID=20111103L;
		
		public boolean extend=false;
		
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
	private WeakComponentClusterer<Object,Integer> clusterer=new WeakComponentClusterer<Object,Integer>();
	
	public ComponentDisambiguator(boolean extend){
		p.extend=extend;
	}
	
	Map<Integer,String> pred;
	ScoreTable<String> amb;

	@Override
	public Map<Integer,String> curate(DirectedGraph<String,Integer> graph){
		
		pred=new HashMap<Integer,String>();
		amb=new ScoreTable<String>();
		
		for(String name:graph.getVertices()){
			// consider only vertices with more than one outgoing edges
			if(graph.getOutEdges(name).size()>1){
				Graph<Object,Integer> subgraph=getSplitSubgraph(graph,name,p.extend);
				Set<Set<Object>> clusters=clusterer.transform(subgraph);
				// only need to output results for seemingly ambiguous names
				if(clusters.size()>1){
					// order the clusters for enumeration
					List<Set<Object>> ordered=new ArrayList<Set<Object>>(clusters);
					for(int i=0;i<ordered.size();i++){
						// go through each vertex
						for(Object vertex:ordered.get(i)){
							// only need to assign split nodes an id
							if(vertex instanceof Integer){
								pred.put((Integer)vertex,CuratorUtil.appendNumber(name,i+1));
							}
						}
					}
					// score ambiguity according number of components
					amb.set(name,clusters.size());
				}
			}
		}
		
		return pred;
	}
	
	@Override
	public ScoreTable<String> getAmbiguityScore(DirectedGraph<String,Integer> graph){
		if(amb==null){
			curate(graph);
		}
		return amb;
	}

	private static Graph<Object,Integer> getSplitSubgraph(Graph<String,Integer> graph,String splittee,boolean extend){

		Graph<Object,Integer> subgraph=new DirectedSparseGraph<Object,Integer>();

		// go through each instance (edge) of the name occurrence
		for(Integer instance:graph.getOutEdges(splittee)){
			// add each instance as its own node in the subgraph; split nodes are ints
			String paper=graph.getDest(instance);
			subgraph.addEdge(subgraph.getEdgeCount(),instance,paper);
			// expand subgraph to other authors(names)
			for(String name:graph.getNeighbors(paper)){
				if(!name.equals(splittee)){
					subgraph.addEdge(subgraph.getEdgeCount(),name,paper);
					// extend to papers written by co-authors as well, if specified
					if(extend){
						for(String epaper:graph.getNeighbors(name)){
							subgraph.addEdge(subgraph.getEdgeCount(),name,epaper);
						}
					}
				}
			}
		}
		
		// remove uninformative papers added by the extension
		if(extend){
			List<Object> toRemove=new ArrayList<Object>(subgraph.getVertexCount());
			for(Object vertex:subgraph.getVertices()){
				if(vertex instanceof String&&subgraph.getPredecessorCount(vertex)==1){
					toRemove.add(vertex);
				}
			}
			for(Object vertex:toRemove){
				subgraph.removeVertex(vertex);
			}
		}

		return subgraph;

	}

}
