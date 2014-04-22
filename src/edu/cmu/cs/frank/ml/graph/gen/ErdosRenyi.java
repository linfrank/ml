package edu.cmu.cs.frank.ml.graph.gen;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import edu.cmu.cs.frank.util.RandomUtil;

/**
 * Generates a random network according to the Erdos-Renyi model
 * 
 * 
 * @author Frank Lin
 */

public class ErdosRenyi{

	private int size;
	private boolean binary;
	private boolean bidirectional;
	
	private int numEdges;

	private Random rand;
	private Set<String> edgeSet;

	public ErdosRenyi(int size,boolean binary,boolean bidirectional){
		this.size=size;
		this.binary=binary;
		numEdges=0;
		rand=new Random();
		edgeSet=new HashSet<String>();
	}
	
	public int size(){
		return size;
	}

	public int numEdges(){
		return numEdges;
	}

	public GenEdge generateEdge(){
		
		int a;
		int b;
		do{
			int[] pair=RandomUtil.pair(size,rand);
			a=pair[0];
			b=pair[1];
		}while(binary&&edgeSet.contains(a+"\t"+b));
		
		GenEdge edge=new GenEdge(a,b,1.0);
		
		if(binary){
			edgeSet.add(a+"\t"+b);
			if(bidirectional){
				edgeSet.add(b+"\t"+a);
			}
		}
		
		numEdges++;
		return edge;

	}

	public static void main(String[] args){

		if(args.length!=3){
			System.out.println("Usage: <size> <edges> <binary?> > output");
			return;
		}

		int size=Integer.parseInt(args[0]);
		int edges=Integer.parseInt(args[1]);
		boolean binary=Boolean.parseBoolean(args[2]);

		ErdosRenyi er=new ErdosRenyi(size,binary,true);
		
		PrintWriter out=new PrintWriter(System.out);
		while(er.numEdges()<edges){
			Util.printEdgeMatlab(er.generateEdge(),1,true,out);
		}
		Util.printEdgeMatlab(size,size,0.0,0,true,out);
		
		out.close();

	}


}