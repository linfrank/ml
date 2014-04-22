package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public abstract class RankSeeder implements Seeder{
	
	public static Logger log=Logger.getLogger(RankSeeder.class);
	
	public static enum Mode {N_PER_CLASS,AT_LEAST_N_PER_CLASS};
	
	private Mode mode;
	private int n;
	
	public RankSeeder(Mode mode,int n){
		this.mode=mode;
		this.n=n;
	}
	
	private static List<LPVertex> topN(List<LPVertex> ranking,int faction,int n){
		List<LPVertex> top=new ArrayList<LPVertex>();
		for(int i=0;top.size()<n&&i<ranking.size();i++){
			LPVertex vertex=ranking.get(i);
			if(vertex.trueLabel==faction){
				top.add(vertex);
			}
		}
		return top;
	}
	
	private static Set<LPVertex> topNPerClass(LPGraph graph,List<LPVertex> ranking,int n){
		Set<LPVertex> seeds=new HashSet<LPVertex>();
		for(int i=0;i<graph.getDimensions();i++){
			List<LPVertex> factionTop=topN(ranking,i,n);
			seeds.addAll(factionTop);
		}
		return seeds;
	}
	
	private static boolean isAtLeastN(int[] counts,int n){
		for(int i=0;i<counts.length;i++){
			if(counts[i]<n){
				return false;
			}
		}
		return true;
	}
	
	private static Set<LPVertex> atLeastNPerClass(LPGraph graph,List<LPVertex> ranking,int n){
		Set<LPVertex> seeds=new HashSet<LPVertex>();
		int[] counts=new int[graph.getDimensions()];
		for(int i=0;i<ranking.size()&&!isAtLeastN(counts,n);i++){
			LPVertex next=ranking.get(i);
			seeds.add(next);
			counts[next.trueLabel]++;
		}
		return seeds;
	}
	
	protected abstract List<LPVertex> getRankings(LPGraph graph);
	
	@Override
	public Set<LPVertex> seed(LPGraph graph){
		
		List<LPVertex> ranking=getRankings(graph);
		
		Set<LPVertex> seeds;
		
		if(mode.equals(Mode.AT_LEAST_N_PER_CLASS)){
			seeds=atLeastNPerClass(graph,ranking,n);
		}
		else{
			seeds=topNPerClass(graph,ranking,n);
		}
		
		return seeds;
		
	}
	
	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(this.getClass().getSimpleName()).append(": Mode=").append(mode).append(" N=").append(n);
		return b.toString();
	}
	
}
