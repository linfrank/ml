package edu.cmu.cs.frank.ml.rank.quality;

import java.util.List;
import java.util.Set;

/**
 * Averaged precision
 * @param <T>
 */

public class AP<T> implements RankQualityMeasure<T>{

	@Override
	public double measure(List<T> ranking,Set<T> relevant){
		
		if(ranking.size()<1){
			return 0.0;
		}
		
		int correct=0;
		double total=0.0;
		for(int i=0;i<ranking.size();i++){
			if(relevant.contains(ranking.get(i))){
				correct++;
				total+=(double)correct/(i+1);
				if(correct>=relevant.size()){
					break;
				}
			}
		}
		
		return total/relevant.size();
	}
	
	@Override
	public String toString(){
		return AP.class.getSimpleName();
	}

}
