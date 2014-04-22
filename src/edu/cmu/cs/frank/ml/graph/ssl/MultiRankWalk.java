package edu.cmu.cs.frank.ml.graph.ssl;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.Parameters;

public class MultiRankWalk extends MultiRankSimple{

	protected static Logger logger=Logger.getLogger(MultiRankWalk.class);

	public static class Params extends MultiRankSimple.Params{
		
		static final long serialVersionUID=20080530L;
		
		public VertexClassifier vertexClassifier=new VertexSimpleClassifier();
		public boolean normalized=true;
		public boolean usingPrior=false;
		
	}
	
	protected Params p;
	
	protected int[] seedCounts;
	protected double[] prior;
	
	@Override
	public void setParams(Parameters params){
		this.p=(Params)params;
	}
	
	@Override
	public Parameters getParams(){
		return p;
	}

	@Override
	public void setGraph(LPGraph graph){
		super.setGraph(graph);
		seedCounts=new int[graph.getDimensions()];
		for(LPVertex vertex:graph.getVertices()){
			if(vertex.seed){
				seedCounts[vertex.trueLabel]++;
				double[] ranks=new double[graph.getDimensions()];
				ranks[vertex.trueLabel]=1.0;
				vertex.scores=ranks;
			}
		}
		if(p.usingPrior){
			logger.info("Calculating prior probability...");
			prior=new double[seedCounts.length];
			int totalSeeds=ArrayUtil.sum(seedCounts);
			for(int i=0;i<seedCounts.length;i++){
				prior[i]=((double)seedCounts[i])/totalSeeds;
			}
		}
	}

	public int[] getSeedCounts(){
		return seedCounts;
	}
	
	@Override
	public void step(){
		logger.debug("t="+t);

		previousField=currentField;
		currentField=getNewField(0.0);
		for(LPVertex vertex:currentField.keySet()){
			for(LPVertex source:graph.getPredecessors(vertex)){
				for(int i=0;i<graph.getDimensions();i++){
					double sourceScore=source.scores[i];
					int sourceOutDegree=graph.outDegree(source);
					if(sourceOutDegree>0){
						currentField.get(vertex)[i]+=sourceScore/sourceOutDegree;
					}
				}
			}
			for(int i=0;i<graph.getDimensions();i++){
				currentField.get(vertex)[i]=p.damper*currentField.get(vertex)[i];
			}
			if(vertex.seed){
				if(p.normalized){
					currentField.get(vertex)[vertex.trueLabel]+=(1.0-p.damper)/seedCounts[vertex.trueLabel];
				}
				else{
					currentField.get(vertex)[vertex.trueLabel]+=(1.0-p.damper);
				}
			}
		}
		if(p.normalized){
			normalizeField(currentField);
		}
		for(LPVertex vertex:currentField.keySet()){
			for(int i=0;i<graph.getDimensions();i++){
				vertex.scores[i]=currentField.get(vertex)[i];
			}
		}

		if(p.vertexClassifier!=null){
			for(LPVertex vertex:graph.getVertices()){
				vertex.predictedLabel=p.vertexClassifier.predict(graph,vertex);
				vertex.confident=p.vertexClassifier.isConfident(graph,vertex);
			}
		}

		t++;
	}

}
