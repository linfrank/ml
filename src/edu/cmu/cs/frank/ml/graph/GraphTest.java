package edu.cmu.cs.frank.ml.graph;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;


public class GraphTest{
	
	public static <V,E> String toString(Graph<V,E> g){
		StringBuilder b=new StringBuilder();
		for(E e:g.getEdges()){
			b.append(g.getSource(e)).append(" --[").append(e).append("]-> ").append(g.getDest(e)).append("\n");
		}
		return b.toString();
	}

	
	public static void main(String[] args){
		
		Graph<String,Double> g=new DirectedSparseGraph<String,Double>();
		
		String v1="Pittsburgh";
		String v2="New York";
		String v3="Boston";
		
		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		
		Double e1=0.3;
		Double e2=0.4;
		Double e3=0.5;
		
		g.addEdge(e1,v1,v2);
		g.addEdge(e2,v2,v3);
		g.addEdge(e3,v3,v2);
		g.addEdge(0.1,"New York","Pittsburgh");
		
		Graph<String,Double> c=GraphUtil.copy(g);
		
		System.out.println(toString(g));
		System.out.println(toString(c));
		
		g.removeVertex(v1);
		
		System.out.println(toString(g));
		System.out.println(toString(c));
		
	}

}
