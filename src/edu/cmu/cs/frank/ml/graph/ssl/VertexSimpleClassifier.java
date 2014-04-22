package edu.cmu.cs.frank.ml.graph.ssl;

import edu.cmu.cs.frank.util.ArrayUtil;
import edu.uci.ics.jung.graph.Graph;

public class VertexSimpleClassifier implements VertexClassifier{

	@Override
	public boolean isConfident(Graph<LPVertex,LPEdge> g,LPVertex vertex){
		return true;
	}

	@Override
	public int predict(Graph<LPVertex,LPEdge> g,LPVertex vertex){
		return ArrayUtil.maxIndex(vertex.scores);
	}

	@Override
	public void signalGraphUpdate(){}

	@Override
	public String toString(){
		return this.getClass().getSimpleName();
	}

}
