package edu.cmu.cs.frank.ml.graph.ssl;


public class MultiRankTrade extends MultiRankSimple{

	public MultiRankTrade(double damper,int maxT,double convergence){
		super(damper,maxT,convergence);
	}

	public MultiRankTrade(double damper,int maxT){
		super(damper,maxT);
	}

	@Override
	public void step(){
		logger.debug("t="+t);
		previousField=currentField;
		currentField=getNewField(0.0);
		for(LPVertex vertex:currentField.keySet()){
			double[] newScore=new double[graph.getDimensions()];
			for(LPVertex source:graph.getPredecessors(vertex)){
				LPEdge edge=graph.findEdge(source,vertex);
				if(edge.alive){
					double sourceScore=source.getTotalScore()/graph.getDimensions();
					int outDegree=source.predictedOutDegree(graph,edge.predictedLabel);
					if(outDegree>0){
						newScore[edge.predictedLabel]+=sourceScore/outDegree;
					}
				}
			}
			for(int i=0;i<graph.getDimensions();i++){
				newScore[i]=(1.0-p.damper)/currentField.size()+p.damper*newScore[i];
			}
			currentField.put(vertex,newScore);
		}
		normalizeField(currentField);
		for(LPVertex vertex:currentField.keySet()){
			vertex.scores=currentField.get(vertex);
		}
		logger.debug(currentField.keySet());
		t++;
	}

}
