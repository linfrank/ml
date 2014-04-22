package edu.cmu.cs.frank.ml.graph;

import java.util.Comparator;

import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.graph.Graph;

public class GraphUtil{
	
	@SuppressWarnings("unchecked")
	public static <V,E> Graph<V,E> copy(Graph<V,E> g){
		try{
			Graph<V,E> c=g.getClass().newInstance();
			for(E e:g.getEdges()){
				c.addEdge(e,g.getSource(e),g.getDest(e));
			}
			return c;
		}catch(Exception e){
			return null;
		}
	}
	
	public static class VertexScoreComparator<V> implements Comparator<V>{
		
		private VertexScorer<V,Double> scorer;
		
		public VertexScoreComparator(VertexScorer<V,Double> scorer){
			this.scorer=scorer;
		}
		
		@Override
		public int compare(V a,V b){
			double diff=scorer.getVertexScore(a)-scorer.getVertexScore(b);
			if(diff>0){
				return 1;
			}
			else if(diff<0){
				return -1;
			}
			else{
				return 0;
			}
		}
	}

}
