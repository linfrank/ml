package edu.cmu.cs.frank.ml.graph.gen;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.IOUtil;
import edu.cmu.cs.frank.util.RandomUtil;

/**
 * Generates k Erdos-Renyi size ~N(n,(n*0.1)^2) networks with uniform noise
 * 
 * @author Frank Lin
 */

public class ErdosRenyiCluster{

	private int k;
	private boolean binary;
	private boolean bidirectional;

	private Random rand;
	private int[] sizes;
	private int[] offsets;
	private int size;
	private double noiseProb;
	
	private int numEdges;
	private Set<String> edgeSet;

	public ErdosRenyiCluster(int n,int k,double noise,boolean binary,boolean bidirectional){
		
		this.k=k;
		this.binary=binary;
		this.bidirectional=bidirectional;
		
		rand=new Random();
		sizes=new int[k];
		offsets=new int[k];
		for(int i=0;i<k;i++){
			sizes[i]=(int)(rand.nextGaussian()*n*0.1+n);
			offsets[i]=i==0?0:offsets[i-1]+sizes[i-1];
		}
		size=ArrayUtil.sum(sizes);
		noiseProb=((double)n*k*n*k-n*n*k)/(n*k*n*k)*(2*noise);
		
		numEdges=0;;
		edgeSet=new HashSet<String>();
	}
	
	public int size(int c){
		return sizes[c];
	}
	
	public int size(){
		return size;
	}

	public int numEdges(){
		return numEdges;
	}
	
	public List<GenEdge> chain(boolean ring){
		List<GenEdge> chain=new ArrayList<GenEdge>(size);
		for(int i=0;i<size-1;i++){
			if(!binary||!remembers(i,i+1)){
				chain.add(new GenEdge(i,i+1,1.0));
				if(binary){
					remember(i,i+1);
				}
			}
		}
		if(ring&&!remembers(size-1,0)){
			chain.add(new GenEdge(size-1,0,1.0));
			if(binary){
				remember(size-1,0);
			}
		}
		return chain;
	}

	public GenEdge generateEdge(){

		int a;
		int b;

		do{
			if(rand.nextDouble()>noiseProb){
				int c=rand.nextInt(k);
				int[] nodes=RandomUtil.pair(sizes[c],rand);
				a=offsets[c]+nodes[0];
				b=offsets[c]+nodes[1];
			}
			else{
				int[] nodes=RandomUtil.pair(size,rand);
				a=nodes[0];
				b=nodes[1];
			}
		}while(binary&&remembers(a,b));

		GenEdge edge=new GenEdge(a,b,1.0);

		if(binary){
			remember(a,b);
		}

		numEdges++;
		return edge;

	}
	
	private boolean remembers(int a,int b){
		return edgeSet.contains(a+"\t"+b);
	}
	
	private void remember(int a,int b){
		edgeSet.add(a+"\t"+b);
		if(bidirectional){
			edgeSet.add(b+"\t"+a);
		}
	}

	public static void main(String[] args)throws IOException{

		if(args.length!=7){
			System.out.println("Usage: <n> <k> <noise> <edges> <binary?> <chain?> <output prefix>");
			return;
		}

		int n=Integer.parseInt(args[0]);
		int k=Integer.parseInt(args[1]);
		double noise=Double.parseDouble(args[2]);
		int edges=Integer.parseInt(args[3]);
		boolean binary=Boolean.parseBoolean(args[4]);
		boolean chain=Boolean.parseBoolean(args[5]);
		String outpref=args[6];
		
		ErdosRenyiCluster er=new ErdosRenyiCluster(n,k,noise,binary,true);
		
		PrintWriter feature=IOUtil.getPrintWriter(outpref+"_feature.data","utf8",true);
		while(er.numEdges()<edges){
			Util.printEdgeMatlab(er.generateEdge(),1,true,feature);
		}		
		if(chain){
			Util.printEdgeMatlab(er.chain(false),1,true,feature);
		}
		Util.printEdgeMatlab(er.size(),er.size(),0.0,0,true,feature);
		feature.close();
		
		PrintWriter label=IOUtil.getPrintWriter(outpref+"_label.data","utf8",true);
		for(int i=0;i<k;i++){
			for(int j=0;j<er.size(i);j++){
				label.println(i+1);
			}
		}
		label.close();

	}


}