package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.cmu.cs.frank.ml.graph.GraphUtil.VertexScoreComparator;
import edu.uci.ics.jung.algorithms.scoring.PageRank;

public class PageRankSeeder extends RankSeeder{
	
	public PageRankSeeder(Mode mode,int n){
		super(mode,n);
	}
	
	@Override
	protected List<LPVertex> getRankings(LPGraph g){
		final PageRank<LPVertex,LPEdge> pr=new PageRank<LPVertex,LPEdge>(g,0.15);
		pr.setMaxIterations(100);
		pr.evaluate();
		List<LPVertex> ranking=new ArrayList<LPVertex>(g.getVertices());
		Collections.sort(ranking,Collections.reverseOrder(new VertexScoreComparator<LPVertex>(pr)));
		return ranking;
	}
	
}
