package edu.cmu.cs.frank.ml.graph.gen;

import java.io.PrintWriter;
import java.util.List;


public class Util{

	public static void printEdgeMatlab(int from,int to,double weight,int offset,boolean bidirectional,PrintWriter out){
		out.print(from+offset);
		out.print("\t");
		out.print(to+offset);
		out.print("\t");
		out.print(weight);
		out.print("\n");
		if(bidirectional&&from!=to){
			out.print(to+offset);
			out.print("\t");
			out.print(from+offset);
			out.print("\t");
			out.print(weight);
			out.print("\n");
		}
		out.flush();
	}
	
	public static void printEdgeMatlab(GenEdge edge,int offset,boolean bidirectional,PrintWriter out){
		printEdgeMatlab(edge.from,edge.to,edge.weight,offset,bidirectional,out);
	}
	
	public static void printEdgeMatlab(List<GenEdge> edges,int offset,boolean bidirectional,PrintWriter out){
		for(GenEdge edge:edges){
			printEdgeMatlab(edge,offset,bidirectional,out);
		}
	}
	
}
