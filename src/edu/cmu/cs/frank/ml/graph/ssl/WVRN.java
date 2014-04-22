package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.Parameters;

/**
 * An implementation of the wvRN classifier using relaxation labeling inference 
 * in Macskassy & Provost 2007
 */

public class WVRN implements LabelPropagator{

	protected static Logger logger=Logger.getLogger(WVRN.class);

	public static class Params extends Parameters{
		
		static final long serialVersionUID=20080530L;
		
		public int maxT=99;
		public VertexClassifier vertexClassifier=new VertexSimpleClassifier();
		
	}
	
	private Params p;
	private LPGraph graph;
	private int t;

	@Override
	public Parameters getParams(){
		return p;
	}

	@Override
	public void setParams(Parameters params){
		this.p=(Params)params;
	}

	@Override
	public LPGraph getGraph(){
		return graph;
	}

	@Override
	public void setGraph(LPGraph graph){
		this.graph=graph;
		int[] seedCounts=new int[graph.getDimensions()];
		for(LPVertex vertex:graph.getVertices()){
			if(vertex.seed){
				seedCounts[vertex.trueLabel]++;
				double[] ranks=new double[graph.getDimensions()];
				ranks[vertex.trueLabel]=1.0;
				vertex.scores=ranks;
			}
		}
		double[] prior=new double[seedCounts.length];
		int totalSeeds=ArrayUtil.sum(seedCounts);
		for(int i=0;i<seedCounts.length;i++){
			prior[i]=((double)seedCounts[i])/totalSeeds;
		}
		logger.info("Prior: "+Arrays.toString(prior));
		for(LPVertex vertex:graph.getVertices()){
			if(!vertex.seed){
				vertex.scores=ArrayUtil.copy(prior);
			}
		}
	}

	@Override
	public Logger getLogger(){
		return logger;
	}

	@Override
	public int getMaxT(){
		return p.maxT;
	}

	@Override
	public int getT(){
		return t;
	}


	@Override
	public double getDamper(){
		return 0.0;
	}


	@Override
	public boolean isConverged(){
		return false;
	}

	@Override
	public void reset(){
		setGraph(graph);
		resetT();
	}

	@Override
	public void resetT(){
		t=0;
	}

	protected Map<LPVertex,double[]> getNewField(double initValue){
		Map<LPVertex,double[]> field=new HashMap<LPVertex,double[]>();
		for(Object k:graph.getVertices()){
			LPVertex key=(LPVertex)k;
			double[] values=new double[graph.getDimensions()];
			Arrays.fill(values,initValue);
			field.put(key,values);
		}
		return field;
	}

	protected void normalizeField(Map<LPVertex,double[]> field){
		double[] sums=new double[graph.getDimensions()];
		for(double[] values:field.values()){
			for(int i=0;i<sums.length;i++){
				sums[i]+=values[i];
			}
		}
		for(LPVertex vertex:field.keySet()){
			double[] values=new double[graph.getDimensions()];
			for(int i=0;i<values.length;i++){
				values[i]=field.get(vertex)[i]/sums[i];
			}
			field.put(vertex,values);
		}
	}

	@Override
	public void step(){
		logger.debug("t="+t);

		// calculate t+1
		Map<LPVertex,double[]> field=getNewField(0.0);
		for(LPVertex vertex:field.keySet()){
			if(!vertex.seed){
				for(int i=0;i<graph.getDimensions();i++){
					double neighborSum=0.0;
					for(LPVertex source:graph.getPredecessors(vertex)){
						neighborSum+=source.scores[i];
					}
					field.get(vertex)[i]=neighborSum/graph.getPredecessorCount(vertex);
				}
				ArrayUtil.normalize(field.get(vertex));
			}
			else{
				field.get(vertex)[vertex.trueLabel]=1.0;
			}
		}

		// update t
		for(LPVertex vertex:field.keySet()){
			for(int i=0;i<graph.getDimensions();i++){
				vertex.scores[i]=field.get(vertex)[i];
			}
		}
		
		// classify
		if(p.vertexClassifier!=null){
			for(LPVertex vertex:graph.getVertices()){
				vertex.predictedLabel=p.vertexClassifier.predict(graph,vertex);
				vertex.confident=p.vertexClassifier.isConfident(graph,vertex);
			}
		}

		t++;
	}

	@Override
	public void run(){
		while(t<p.maxT){
			step();
		}
	}

	@Override
	public void run(int numSteps){
		for(int i=0;i<numSteps;i++){
			step();
		}
	}





}
