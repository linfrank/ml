package edu.cmu.cs.frank.ml.graph.gen;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.IOUtil;
import edu.cmu.cs.frank.util.RandomUtil;

/**
 * Generates k Barabasi-Albert networks of size ~N(n,(n*0.1)^2) with uniform noise
 * 
 * 
 * @author Frank Lin
 */

public class BarabasiAlbertCluster1{

	public static void main(String[] args){

		if(args.length!=7){
			System.out.println("Usage: <m0> <m> <n> <k> <noise> <binary?> <output prefix>");
			return;
		}

		int m0=Integer.parseInt(args[0]);
		int m=Integer.parseInt(args[1]);
		int n=Integer.parseInt(args[2]);
		int k=Integer.parseInt(args[3]);
		double noise=Double.parseDouble(args[4]);
		boolean binary=Boolean.parseBoolean(args[5]);
		String outpref=args[6];

		Random rand=new Random();
		int[] sizes=new int[k];
		int[] offsets=new int[k];
		for(int i=0;i<k;i++){
			sizes[i]=(int)(rand.nextGaussian()*n*0.1+n);
			offsets[i]=i==0?0:offsets[i-1]+sizes[i-1];
		}
		int size=ArrayUtil.sum(sizes);

		int numEdges=0;
		Set<String> edgeSet=new HashSet<String>();

		PrintWriter feature=IOUtil.getPrintWriter(outpref+"_feature.data","utf8",true);

		for(int i=0;i<k;i++){
			BarabasiAlbert ba=new BarabasiAlbert(m0,m,binary);
			while(ba.size()<sizes[i]){
				List<GenEdge> edges=ba.growNode(); 
				Util.printEdgeMatlab(edges,offsets[i]+1,true,feature);
				if(binary){
					for(GenEdge edge:edges){
						edgeSet.add(edge.from+"\t"+edge.to);
						edgeSet.add(edge.to+"\t"+edge.from);
					}
				}
			}
			numEdges+=ba.getTotalDegree()/2;
		}

		double noiseProb=((double)n*k*n*k-n*n*k)/(n*k*n*k)*(2*noise);
		int numNoise=(int)(noiseProb*numEdges/(1.0-noiseProb));

		int count=0;
		while(count<numNoise){
			int[] nodes=RandomUtil.pair(size,rand);
			int a=nodes[0];
			int b=nodes[1];
			if(!binary||!edgeSet.contains(a+"\t"+b)){
				Util.printEdgeMatlab(a,b,1.0,1,true,feature);
				if(binary){
					edgeSet.add(a+"\t"+b);
					edgeSet.add(b+"\t"+a);
				}
				count++;
			}
		}
		Util.printEdgeMatlab(size,size,0.0,0,true,feature);
		feature.close();
		
		PrintWriter label=IOUtil.getPrintWriter(outpref+"_label.data","utf8",true);
		for(int i=0;i<k;i++){
			for(int j=0;j<sizes[i];j++){
				label.println(i+1);
			}
		}
		label.close();

	}


}