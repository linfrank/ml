package edu.cmu.cs.frank.x;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;


public class CuratorUtil{
	
	public static String appendNumber(String name,Number number){
		return (new StringBuilder(name)).append("[").append(number).append("]").toString();
	}
	
	public static UndirectedGraph<String,Integer> toCoAuthorGraph(DirectedGraph<String,Integer> authorPaperGraph){
		
		UndirectedGraph<String,Integer> coAuthorGraph=new UndirectedSparseGraph<String,Integer>();
		
		for(String author:authorPaperGraph.getVertices()){
			for(String paper:authorPaperGraph.getSuccessors(author)){
				for(String coAuthor:authorPaperGraph.getPredecessors(paper)){
					if(!coAuthor.equals(author)){
						coAuthorGraph.addEdge(coAuthorGraph.getEdgeCount(),author,coAuthor);
					}
				}
			}
		}
		
		return coAuthorGraph;
		
	}

}
