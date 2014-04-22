package edu.cmu.cs.frank.ml.graph.ssl;

import org.apache.log4j.Logger;

/**
 * Difference between this and MultiRankWalk - this one only walks according to edge factions
 * 
 * @author Frank Lin
 */

public class MultiRankWalk2 extends MultiRankWalk{

	protected static Logger logger=Logger.getLogger(MultiRankWalk2.class);
	
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
				currentField.get(vertex)[i]=p.damper*currentField.get(vertex)[i];
			}
			if(vertex.seed){
				currentField.get(vertex)[vertex.trueLabel]+=(1.0-p.damper)/seedCounts[vertex.trueLabel];
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

}
