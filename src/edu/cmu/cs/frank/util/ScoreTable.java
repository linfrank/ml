package edu.cmu.cs.frank.util;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScoreTable<T> extends LinkedHashMap<T,Double> implements Serializable{
	
	static final long serialVersionUID=20080319L;
	
	public ScoreTable(){
		super();
	}
	
	public ScoreTable(int initCap){
		super(initCap);
	}

	public void set(T key,double score){
		put(key,score);
	}

	@Override
	public Double get(Object key){
		Double val=super.get(key);
		return val==null?0.0:val;
	}

	public void increment(T key,double increment){
		set(key,get(key)+increment);
	}

	public void increment(T key){
		increment(key,1.0);
	}

	public void mergeAdd(ScoreTable<T> other){
		for(T key:other.keySet()){
			put(key,get(key)+other.get(key));
		}
	}

	public void mergeMultiply(ScoreTable<T> other){
		for(T key:other.keySet()){
			put(key,get(key)*other.get(key));
		}
	}
	
	public List<T> keyList(){
		return new ArrayList<T>(keySet());
	}
	
	public Collection<Double> scores(){
		return values();
	}
	
	public T highestKey(){
		T hKey=null;
		double hScore=Double.MIN_VALUE;
		for(Map.Entry<T,Double> entry:entrySet()){
			if(entry.getValue()>hScore){
				hKey=entry.getKey();
				hScore=entry.getValue();
			}
		}
		return hKey;
	}
	
	public T lowestKey(){
		T lKey=null;
		double lScore=Double.MAX_VALUE;
		for(Map.Entry<T,Double> entry:entrySet()){
			if(entry.getValue()<lScore){
				lKey=entry.getKey();
				lScore=entry.getValue();
			}
		}
		return lKey;
	}

	public List<T> sortedKeys(boolean ascend){
		List<T> list=new ArrayList<T>(keySet());
		if(ascend){
			Collections.sort(list,new MapUtil.MapComparator<T,Double>(this));
		}
		else{
			Collections.sort(list,Collections.reverseOrder(new MapUtil.MapComparator<T,Double>(this)));
		}
		return list;
	}

	public List<T> sortedKeysAscend(){
		return sortedKeys(true);
	}

	public List<T> sortedKeysDescend(){
		return sortedKeys(false);
	}

	public List<T> topKeys(int n,boolean ascend){
		List<T> list=sortedKeys(ascend);
		return list.subList(0,n);
	}

	public List<T> topKeysAscend(int n){
		return topKeys(n,true);
	}
	
	public List<T> topKeysDescend(int n){
		return topKeys(n,false);
	}
	
	public double[] scoresArray(){
		double[] scores=new double[size()];
		int i=0;
		for(Double value:values()){
			scores[i]=value;
			i++;
		}
		return scores;
	}
	
	public static <T> ScoreTable<T> average(Collection<ScoreTable<T>> tables){
		
		ScoreTable<T> avg=new ScoreTable<T>();
		for(ScoreTable<T> table:tables){
			avg.mergeAdd(table);
		}
		for(Map.Entry<T,Double> entry:avg.entrySet()){
			entry.setValue(entry.getValue()/tables.size());
		}
		return avg;
		
	}
	
	public static <T> ScoreTable<T> stdDev(Collection<ScoreTable<T>> tables){
		
		ScoreTable<T> mean=average(tables);
		ScoreTable<T> stdDev=new ScoreTable<T>();
		for(T key:mean.keyList()){
			double devSum=0.0;
			for(ScoreTable<T> table:tables){
				double diff=table.get(key)-mean.get(key);
				devSum+=diff*diff;
			}
			stdDev.set(key,Math.sqrt(devSum/tables.size()));
		}
		return stdDev;
		
	}
	
	public String toStringNice(){
		int maxLength=0;
		for(T key:keySet()){
			int length=key.toString().length();
			if(length>maxLength){
				maxLength=length;
			}
		}
		StringBuilder b=new StringBuilder();
		for(T key:keySet()){
			b.append(TextUtil.padRight(key.toString(),' ',maxLength+1)).append(get(key)).append("\n");
		}
		return b.toString();
	}

}
