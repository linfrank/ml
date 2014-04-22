package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.graph.Graph;

public class PageRank{

	protected static Logger logger=Logger.getLogger(PageRank.class);

	protected Graph<LPVertex,LPEdge> graph;
	protected int maxT;
	protected double damper;
	
	protected int t;
	protected Map<LPVertex,Double> field;

	public PageRank(Graph<LPVertex,LPEdge> graph,double damper,int maxT){
		this.graph=graph;
		this.damper=damper;
		this.maxT=maxT;
		t=0;
		field=new HashMap<LPVertex,Double>();
		for(Object o:graph.getVertices()){
			field.put((LPVertex)o,0.0);
		}
	}

	public Graph<LPVertex,LPEdge> getGraph(){
		return graph;
	}
	public void setGraph(Graph<LPVertex,LPEdge> graph){
		this.graph=graph;
	}
	public int getMaxT(){
		return maxT;
	}
	public void setMaxT(int maxT){
		this.maxT=maxT;
	}
	public double getDamper(){
		return damper;
	}
	public void setDamper(double damper){
		this.damper=damper;
	}

	public void step(){
		logger.info("t="+t);
		initializeField(0.0);
		for(LPVertex vertex:field.keySet()){
			for(Object o:graph.getPredecessors(vertex)){
				LPVertex source=(LPVertex)o;
				double sourceScore=source.getTotalScore();
				int outDegree=graph.outDegree(source);
				field.put(vertex,field.get(vertex)+(sourceScore/outDegree));
			}
		}
		normalizeField();
		for(LPVertex vertex:field.keySet()){
			field.put(vertex,(1.0-damper)/field.size()+damper*field.get(vertex));
		}
		for(LPVertex vertex:field.keySet()){
			vertex.scores[0]=field.get(vertex);
		}
		logger.info(field.keySet());
		t++;
	}

	protected void normalizeField(){
		double sum=0.0;
		for(double score:field.values()){
			sum+=score;
		}
		for(LPVertex vertex:field.keySet()){
			field.put(vertex,field.get(vertex)/sum);
		}
	}
	
	protected void initializeField(double value){
		for(LPVertex vertex:field.keySet()){
			field.put(vertex,value);
		}
	}
	
	public void run(int steps){
		System.out.println("Running "+steps+" iterations...");
		for(int i=0;i<steps;i++){
			step();
		}
	}

	public void run(){
		System.out.println("Running "+maxT+" iterations...");
		while(t<maxT){
			step();
		}
	}

	public void reset(){
		System.out.println("Reseting graph.");
		for(LPVertex v:graph.getVertices()){
			double[] scores=new double[1];
			scores[0]=1.0/graph.getVertexCount();
			v.scores=scores;
		}
		t=0;
	}

}
