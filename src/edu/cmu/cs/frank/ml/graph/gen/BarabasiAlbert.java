package edu.cmu.cs.frank.ml.graph.gen;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Generates a scale-free network according to the Barabasi-Albert model.
 * 
 * 
 * @author Frank Lin
 */

public class BarabasiAlbert{

	private int m0;
	private int m;
	private boolean binary;

	private List<Integer> degree;
	private int totalDegree;

	private Random rand;
	private Set<Integer> edgeSet;

	public BarabasiAlbert(int m0,int m,boolean binary){
		this.m0=m0;
		this.m=m;
		this.binary=binary;
		degree=new ArrayList<Integer>();
		totalDegree=0;
		rand=new Random();
		edgeSet=new HashSet<Integer>();
	}

	public int size(){
		return degree.size();
	}

	public int getDegree(int node){
		return degree.get(node);
	}

	public int getTotalDegree(){
		return totalDegree;
	}

	public List<GenEdge> growNode(){

		List<GenEdge> edges=new ArrayList<GenEdge>(m);
		int node=degree.size();
		degree.add(0);

		if(node<m0){
			for(int i=0;i<node;i++){
				edges.add(new GenEdge(node,i,1.0));
				degree.set(node,degree.get(node)+1);
				degree.set(i,degree.get(i)+1);
				totalDegree+=2;
			}
		}
		else{

			if(binary){
				edgeSet.clear();
			}
			
			while(degree.get(node)<m){
				int cand=rand.nextInt(node);
				if(!binary||!edgeSet.contains(cand)){
					double prob=((double)degree.get(cand))/totalDegree;
					if(rand.nextDouble()<prob){
						edges.add(new GenEdge(node,cand,1.0));
						degree.set(node,degree.get(node)+1);
						degree.set(cand,degree.get(cand)+1);
						totalDegree+=2;
						if(binary){
							edgeSet.add(cand);
						}
					}
				}
			}

		}

		return edges;

	}

	public static void main(String[] args){

		if(args.length!=4){
			System.out.println("Usage: <size> <m0> <m> <binary?> > output");
			return;
		}

		int size=Integer.parseInt(args[0]);
		int m0=Integer.parseInt(args[1]);
		int m=Integer.parseInt(args[2]);
		boolean binary=Boolean.parseBoolean(args[3]);

		BarabasiAlbert ba=new BarabasiAlbert(m0,m,binary);
		
		PrintWriter out=new PrintWriter(System.out);
		
		while(ba.size()<size){
			List<GenEdge> edges=ba.growNode();
			Util.printEdgeMatlab(edges,1,true,out);
		}
		Util.printEdgeMatlab(size,size,0.0,0,true,out);
		
		out.close();

	}


}