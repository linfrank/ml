package edu.cmu.cs.frank.ml.rank.quality;

import java.util.List;
import java.util.Set;

import edu.cmu.cs.frank.util.FormatUtil;

/**
 * Precision at a specific number/percentage of recall
 * @param <T>
 */

public class PrecisionAtRecall<T> implements RankQualityMeasure<T>{

	private Number level;
	
	public PrecisionAtRecall(int num){
		level=num;
	}
	
	public PrecisionAtRecall(double percent){
		level=percent;
	}
	
	@Override
	public double measure(List<T> ranking,Set<T> relevant){
		
		if(ranking.size()<1){
			return 0.0;
		}

		int stop=0;
		if(level instanceof Integer){
			stop=(Integer)level;
		}
		else if(level instanceof Double){
			stop=(int)((Double)level*relevant.size());
		}
		stop=Math.min(stop,relevant.size());

		int correct=0;
		for(int i=0;i<ranking.size();i++){
			if(relevant.contains(ranking.get(i))){
				correct++;
				if(correct>=stop){
					return (double)correct/(i+1);
				}
			}
		}
		
		return 0.0;
	}
	
	@Override
	public String toString(){
		if(level instanceof Integer){
			return "P@"+level;
		}
		else if(level instanceof Double){
			return "P@"+FormatUtil.s3(((Double)level)*100)+"%";
		}
		else{
			return PrecisionAtRecall.class.getSimpleName();
		}
	}

}
