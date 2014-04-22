package edu.cmu.cs.frank.ml.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.graph.Graph;

public interface GraphModifier<V,E>{

	public static final Logger log=Logger.getLogger(GraphModifier.class);

	public void modify(Graph<V,E> g);

	public class Identity<V,E> implements GraphModifier<V,E>{
		@Override
		public void modify(Graph<V,E> g){}
	}

	public class ReverseEdge<V,E> implements GraphModifier<V,E>{
		@Override
		public void modify(Graph<V,E> g){
			for(E e:g.getEdges()){
				V source=g.getSource(e);
				V dest=g.getDest(e);
				g.removeEdge(e);
				g.addEdge(e,dest,source);
			}
		}
	}

	public class RandomDegreeCap<V,E> implements GraphModifier<V,E>{
		private int cap;
		public RandomDegreeCap(int cap){
			this.cap=cap;
		}
		@Override
		public void modify(Graph<V,E> g){
			int removed=0;
			for(V v:g.getVertices()){
				if(g.degree(v)>cap){
					List<E> candidates=new ArrayList<E>(g.getIncidentEdges(v));
					Collections.shuffle(candidates);
					for(int i=0;i<(g.degree(v)-cap);i++){
						g.removeEdge(candidates.get(i));
						removed++;
					}
				}
			}
			log.info("Removed "+removed+" edges.");
		}
	}

}
