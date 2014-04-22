package edu.cmu.cs.frank.x;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.Parameters;
import edu.cmu.cs.frank.util.ScoreTable;
import edu.uci.ics.jung.graph.DirectedGraph;


public class RandomDisambiguator implements Curator{

	static Logger log=Logger.getLogger(RandomDisambiguator.class);

	public static class Params extends Parameters{
		
		static final long serialVersionUID=20111103L;
		
	}
	
	@Override
	public void setParams(Parameters p){
		this.p=(Params)p;
	}

	@Override
	public Parameters getParams(){
		return p;
	}

	private Params p=new Params();

	@Override
	public Map<Integer,String> curate(DirectedGraph<String,Integer> graph){
		Map<Integer,String> pred=new HashMap<Integer,String>();
		return pred;
	}
	
	@Override
	public ScoreTable<String> getAmbiguityScore(DirectedGraph<String,Integer> graph){
		
		ScoreTable<String> degrees=new ScoreTable<String>(graph.getVertexCount()/2);
		
		Random rand=new Random();
		
		for(String name:graph.getVertices()){
			if(name.startsWith("N:")&&graph.degree(name)>1){
				degrees.set(name,rand.nextDouble());
			}
		}
		
		return degrees;
		
	}

}
