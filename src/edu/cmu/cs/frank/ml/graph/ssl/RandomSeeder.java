package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomSeeder extends RankSeeder{
	
	public RandomSeeder(Mode mode,int n){
		super(mode,n);
	}
	
	@Override
	protected List<LPVertex> getRankings(LPGraph graph){
		List<LPVertex> rankings=new ArrayList<LPVertex>(graph.getVertices());
		Collections.shuffle(rankings);
		return rankings;
	}

}
