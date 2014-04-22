package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.graph.Graph;

public abstract class AbstractVertexClassifier implements VertexClassifier{
	
	protected boolean graphUpdated;
	protected Map<LPVertex,Integer> labels;
	protected Map<LPVertex,Boolean> confidences;
	
	public AbstractVertexClassifier(){
		graphUpdated=true;
	}
	
	@Override
	public int predict(Graph<LPVertex,LPEdge> g,LPVertex v){
		if(graphUpdated){
			reset();
			graphUpdated=false;
		}
		if(!labels.containsKey(v)){
			makePrediction(g,v);
		}
		return labels.get(v);
	}
	
	@Override
	public boolean isConfident(Graph<LPVertex,LPEdge> g,LPVertex v){
		if(graphUpdated){
			reset();
			graphUpdated=false;
		}
		if(!confidences.containsKey(v)){
			makePrediction(g,v);
		}
		return confidences.get(v);
	}
	
	@Override
	public void signalGraphUpdate(){
		graphUpdated=true;
	}
	
	private void reset(){
		labels=new HashMap<LPVertex,Integer>();
		confidences=new HashMap<LPVertex,Boolean>();
	}
	
	protected abstract void makePrediction(Graph<LPVertex,LPEdge> g,LPVertex v);
	
	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}

}
