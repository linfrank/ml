package edu.cmu.cs.frank.ml.rank.quality;

import java.util.List;
import java.util.Set;


public interface RankQualityMeasure<T>{
	
	public double measure(List<T> ranking,Set<T> relevant);

}
