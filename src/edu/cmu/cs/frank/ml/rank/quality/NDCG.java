package edu.cmu.cs.frank.ml.rank.quality;

import java.util.List;
import java.util.Set;

import edu.cmu.cs.frank.util.MathUtil;

/**
 * Normalized discounted cumulative gain
 * @param <T>
 */

public class NDCG<T> implements RankQualityMeasure<T>{

	@Override
	public double measure(List<T> ranking,Set<T> relevant){
		
		if(ranking.size()<1){
			return 0.0;
		}
		
		double dcg=0.0;
		double idcg=0.0;
		for(int i=0;i<ranking.size();i++){
			double gain=i==0?1.0:1.0/MathUtil.log2(i+1);
			if(relevant.contains(ranking.get(i))){
				dcg+=gain;
			}
			idcg+=gain;
		}
		
		return dcg/idcg;
	}
	
	@Override
	public String toString(){
		return NDCG.class.getSimpleName();
	}

}
