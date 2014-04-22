package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.List;

import edu.cmu.cs.frank.util.ScoreTable;

public class InLinkSeeder extends RankSeeder{
	
	public InLinkSeeder(Mode mode,int n){
		super(mode,n);
	}
	
	@Override
	protected List<LPVertex> getRankings(LPGraph g){
		
		ScoreTable<LPVertex> table=new ScoreTable<LPVertex>();
		
		for(LPVertex v:g.getVertices()){
			table.set(v,g.getPredecessorCount(v));
		}
		
		return table.sortedKeysDescend();
	}

}
