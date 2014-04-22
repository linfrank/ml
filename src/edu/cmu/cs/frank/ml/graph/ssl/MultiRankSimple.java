package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.Parameters;

public class MultiRankSimple implements LabelPropagator{

	protected static Logger logger=Logger.getLogger(MultiRankSimple.class);

	public static class Params extends Parameters{
		
		static final long serialVersionUID=20080530L;
		
		public double damper=0.85;
		public int maxT=200;
		public double convergence=0.0001;
		
	}
	
	protected Params p;

	protected LPGraph graph;

	protected int t;
	protected Map<LPVertex,double[]> previousField;
	protected Map<LPVertex,double[]> currentField;

	public MultiRankSimple(){
		p=new Params();
		t=0;
	}
	
	public MultiRankSimple(double damper,int maxT){
		this();
		p.damper=damper;
		p.maxT=maxT;
	}
	
	public MultiRankSimple(double damper,int maxT,double convergence){
		this(damper,maxT);
		p.convergence=convergence;
	}

	@Override
	public void setParams(Parameters params){
		this.p=(Params)params;
	}
	
	@Override
	public Parameters getParams(){
		return p;
	}

	@Override
	public LPGraph getGraph(){
		return graph;
	}
	@Override
	public void setGraph(LPGraph graph){
		this.graph=graph;
	}
	@Override
	public int getMaxT(){
		return p.maxT;
	}
	public void setMaxT(int maxT){
		this.p.maxT=maxT;
	}
	@Override
	public double getDamper(){
		return p.damper;
	}
	public void setDamper(double damper){
		this.p.damper=damper;
	}

	@Override
	public void step(){
		logger.debug("t="+t);

		previousField=currentField;
		currentField=getNewField(0.0);
		for(LPVertex vertex:currentField.keySet()){
			for(LPVertex source:graph.getPredecessors(vertex)){
				LPEdge edge=graph.findEdge(source,vertex);
				if(edge.alive){
					double sourceScore=source.scores[edge.predictedLabel];
					int sourceOutDegree=source.predictedOutDegree(graph,edge.predictedLabel);
					if(sourceOutDegree>0){
						currentField.get(vertex)[edge.predictedLabel]+=sourceScore/sourceOutDegree;
					}
				}
			}
			for(int i=0;i<graph.getDimensions();i++){
				currentField.get(vertex)[i]=(1.0-p.damper)/currentField.size()+p.damper*currentField.get(vertex)[i];
			}
		}
		normalizeField(currentField);
		for(LPVertex vertex:currentField.keySet()){
			for(int i=0;i<graph.getDimensions();i++){
				vertex.scores[i]=currentField.get(vertex)[i];
			}
		}
		t++;

		logger.debug(currentField.keySet());
	}

	@Override
	public void run(int steps){
		logger.info("Running "+steps+" iterations...");
		for(int i=0;i<steps;i++){
			step();
		}
		logger.info("t="+t);
	}

	@Override
	public void run(){
		logger.info("Running until convergence...");
		step();
		while(!isConverged()&&t<p.maxT){
			step();
		}
		if(t>=p.maxT){
			logger.info("Did not converge; terminating early at t="+t);
		}
		else{
			logger.info("Converged at t="+t);
		}
	}

	protected boolean isConverged(Map<LPVertex,double[]> current,Map<LPVertex,double[]> previous){
		if(current==null||previous==null){
			return false;
		}
		else{
			double dist[]=new double[graph.getDimensions()];
			for(Object v:graph.getVertices()){
				double[] currValues=current.get(v);
				double[] prevValues=previous.get(v);
				for(int i=0;i<graph.getDimensions();i++){
					dist[i]+=Math.pow(Math.abs(currValues[i]-prevValues[i]),2);
				}
			}
			for(int i=0;i<graph.getDimensions();i++){
				dist[i]=Math.sqrt(dist[i]);
			}
			for(int i=0;i<graph.getDimensions();i++){
				if(dist[i]>p.convergence){
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public boolean isConverged(){
		return isConverged(currentField,previousField);
	}

	@Override
	public void reset(){
		for(LPVertex vertex:graph.getVertices()){
			double[] ranks=new double[graph.getDimensions()];
			Arrays.fill(ranks,1.0/graph.getVertexCount());
			vertex.scores=ranks;
			vertex.seed=false;
			vertex.confident=false;
			vertex.predictedLabel=-1;
		}
		t=0;
		logger.info("MultiRank reset.");
	}

	@Override
	public int getT(){
		return t;
	}

	@Override
	public void resetT(){
		t=0;
	}

	@Override
	public Logger getLogger(){
		return logger;
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



}
