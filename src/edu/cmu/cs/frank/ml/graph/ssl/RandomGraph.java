package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.cmu.cs.frank.util.ArrayUtil;
import edu.uci.ics.jung.graph.Graph;

public class RandomGraph{

	private static Random rand=new Random();

//	public static void makeCompleteMultiGraph(DirectedGraph graph){
//		List<MultiVertex> vertices=new ArrayList<MultiVertex>(graph.getVertices());
//		for(int i=0;i<vertices.size();i++){
//			for(int j=0;j<vertices.size();j++){
//				if(i!=j){
//					MultiVertex v1=vertices.get(i);
//					MultiVertex v2=vertices.get(j);
//					graph.addEdge(new MultiEdge(v1,v2,v2.getTrueLabel()));
//					graph.addEdge(new MultiEdge(v2,v1,v1.getTrueLabel()));
//				}
//			}
//		}
//	}
//
//	public static boolean isLastConnectingEdge(MultiEdge edge){
//		if(edge.getSource().degree()==1||edge.getDest().degree()==1){
//			return true;
//		}
//		else{
//			return false;
//		}
//	}
//
//	public static void generateRandomMultiEdges(DirectedGraph graph,int numEdges,boolean connected){
//		makeCompleteMultiGraph(graph);
//		List<MultiEdge> queue=new LinkedList<MultiEdge>(graph.getEdges());
//		Collections.shuffle(queue,RAND);
//		while(queue.size()>numEdges){
//			MultiEdge candidate=queue.remove(0);
//			if(connected){
//				while(isLastConnectingEdge(candidate)){
//					queue.add(candidate);
//					candidate=queue.remove(0);
//				}
//			}
//			graph.removeEdge(candidate);
//		}
//	}
//
//	public static void generateRandomMultiEdges(DirectedGraph graph,int numEdges){
//		List<MultiVertex> vertices=new ArrayList<MultiVertex>(graph.getVertices());
//		for(int i=0;i<vertices.size();i++){
//			for(int j=0;j<vertices.size();j++){
//				if(i!=j){
//					MultiVertex v1=vertices.get(i);
//					MultiVertex v2=vertices.get(j);
//					graph.addEdge(new MultiEdge(v1,v2,v2.getTrueLabel()));
//					graph.addEdge(new MultiEdge(v2,v1,v1.getTrueLabel()));
//				}
//			}
//		}
//	}

//	public static void generateRandomMultiVertices(DirectedGraph graph,int numVertices,int numFactions,int factionDev,Random rand){
//		List<MultiVertex> vertices=new ArrayList<MultiVertex>();
//		int totalCurrentPop=0;
//		for(int i=0;i<numFactions;i++){
//			// calculate population for this faction
//			int pop;
//			if(i<numFactions-1){
//				pop=numVertices/numFactions+rand.nextInt(factionDev*2+1)-factionDev;
//				totalCurrentPop+=pop;
//			}
//			else{
//				pop=numVertices-totalCurrentPop;
//			}
//			// generate vertices for this faction
//			for(int j=0;j<pop;j++){
//				// initialize scores
//				double[] scores=new double[numFactions];
//				Arrays.fill(scores,1.0/numVertices);
//				// create vertex in graph
//				vertices.add((MultiVertex)graph.addVertex(new MultiVertex(i,scores)));
//			}
//		}
//	}

	public static void generateRandomMultiEdges(
			Graph<LPVertex,LPEdge> g,
			List<LPVertex> source,
			List<LPVertex> target,
			int numEdges,
			boolean alive,
			boolean predictionCorrect){

		// add all possible candidates
		List<LPVertex[]> candidates=new ArrayList<LPVertex[]>();
		for(int i=0;i<source.size();i++){
			for(int j=0;j<target.size();j++){
				if(i!=j||source!=target){
					candidates.add(new LPVertex[]{source.get(i),target.get(j)});
				}
			}
		}
		// shuffle
		Collections.shuffle(candidates,rand);
		// make the first numEdges edges
		for(int i=0;i<numEdges;i++){
			LPVertex[] pair=candidates.get(i);
			int prediction=-1;
			if(predictionCorrect){
				prediction=pair[1].trueLabel;
			}
			g.addEdge(new LPEdge(String.valueOf(i),1.0,pair[1].trueLabel,alive,prediction),pair[0],pair[1]);
		}

	}

	public static void generateMultiEdge(
			String id,
			Graph<LPVertex,LPEdge> g,
			LPVertex v1,
			LPVertex v2,
			boolean alive,
			boolean predictionCorrect){
		LPVertex source;
		LPVertex target;
		if(rand.nextBoolean()){
			source=v1;
			target=v2;
		}
		else{
			source=v2;
			target=v1;
		}
		int prediction=-1;
		if(predictionCorrect){
			prediction=target.trueLabel;
		}
		g.addEdge(new LPEdge(id,1.0,target.trueLabel,alive,prediction),source,target);
	}
	
	/**
	 * Generates a random MultiGraph with the following properties:
	 * 
	 * @param totalVertices the total number of vertices in the graph
	 * @param totalEdges the total number of edges in the graph - the resulting MultiGraph may have a slightly different number of edges
	 * @param vertexRatio the ratio of vertices of different faction
	 * @param edgeRatio the ratio of edges connecting different factions; the first array dimension indicates the source faction, the second array dimension indicates the target faction
	 * @param connect whether to make sure the graph is connected
	 * @param alive whether to initialize the edges as alive
	 * @param vertexPredictionCorrect whether to initialize the vertices with correct predicted labels
	 * @param edgePredictionCorrect whether to initalize the edges with correct predicted labels
	 */

	public static LPGraph generateRandomMultiGraph(
			int totalVertices,
			int totalEdges,
			double[] vertexRatio,
			double[][] edgeRatio,
			boolean connect,
			boolean alive,
			boolean vertexPredictionCorrect,
			boolean edgePredictionCorrect){
		
		// dimension we're working with
		int dim=vertexRatio.length;

		// determine the number of vertices in each faction
		int[] numVertices=new int[dim];
		ArrayUtil.normalize(vertexRatio);
		for(int i=0;i<dim;i++){
			if(i<dim-1){
				numVertices[i]=(int)(vertexRatio[i]*totalVertices);
			}
			else{
				numVertices[i]=totalVertices-ArrayUtil.sum(numVertices);
			}
		}
		
		// determine the number edges going from every faction to every faction
		int[][] numEdges=new int[dim][dim];
		for(int i=0;i<edgeRatio.length;i++){
			ArrayUtil.normalize(edgeRatio[i]);
		}
		for(int i=0;i<vertexRatio.length;i++){
			for(int j=0;j<dim;j++){
				numEdges[i][j]=(int)(edgeRatio[i][j]*vertexRatio[i]*totalEdges);
			}
		}
		
		// create new MultiGraph
		LPGraph g=new LPGraph(dim);

		// generate vertices for each faction
		List<List<LPVertex>> vertices=new ArrayList<List<LPVertex>>();
		for(int i=0;i<dim;i++){
			vertices.add(new ArrayList<LPVertex>(numVertices[i]));
			for(int j=0;j<numVertices[i];j++){
				double[] initialRanks=new double[dim];
				Arrays.fill(initialRanks,1.0/totalVertices);
				LPVertex v=new LPVertex(String.valueOf(g.getVertexCount()),i,initialRanks);
				g.addVertex(v);
				if(vertexPredictionCorrect){
					v.predictedLabel=v.trueLabel;
				}
				vertices.get(i).add(v);
			}
		}
		
		// generate edges going from each faction to each faction
		for(int i=0;i<dim;i++){
			for(int j=0;j<dim;j++){
				generateRandomMultiEdges(g,vertices.get(i),vertices.get(j),numEdges[i][j],alive,edgePredictionCorrect);
			}
		}

		// connect loose vertices
		if(connect){
			int extraEdges=0;
			for(int i=0;i<vertices.size();i++){
				for(LPVertex v:vertices.get(i)){
					if(g.getIncidentEdges(v).size()==0){
						int otherFaction=randomFaction(edgeRatio[v.trueLabel]);
						int otherFactionSize=vertices.get(otherFaction).size();
						LPVertex other=vertices.get(otherFaction).get(rand.nextInt(otherFactionSize));
						if(rand.nextBoolean()){
							generateMultiEdge(String.valueOf(g.getVertexCount()),g,v,other,alive,edgePredictionCorrect);
						}
						else{
							generateMultiEdge(String.valueOf(g.getVertexCount()),g,other,v,alive,edgePredictionCorrect);
						}
						extraEdges++;
					}
				}
			}
			System.out.println("Added "+extraEdges+" extra edges to connect loose vertices");
		}

		return g;
	}
	
	public static LPGraph generateRandomMultiGraph(
			int totalVertices,
			int totalEdges,
			double[] vertexRatio,
			double[][] edgeRatio){
		return generateRandomMultiGraph(totalVertices,totalEdges,vertexRatio,edgeRatio,true,false,false,false);
	}
	
	private static int randomFaction(double[] edgeRatio){
		ArrayUtil.normalize(edgeRatio);
		double indicator=rand.nextDouble();
		double sum=0;
		for(int i=0;i<edgeRatio.length;i++){
			sum+=edgeRatio[i];
			if(indicator<sum){
				return i;
			}
		}
		return edgeRatio.length-1;
	}

}
