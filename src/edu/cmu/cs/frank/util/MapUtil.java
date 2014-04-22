package edu.cmu.cs.frank.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MapUtil{

	public static void increment(Map<String,Integer> map,String key,int increase){
		if(map.containsKey(key)){
			map.put(key,map.get(key)+increase);
		}
		else{
			map.put(key,increase);
		}
	}

	public static void increment(Map<String,Integer> map,String key){
		increment(map,key,1);
	}

	public static void increment(Map<String,Map<String,Integer>> map,String key1,String key2,int increase){
		if(map.containsKey(key1)){
			increment(map.get(key1),key2,increase);
		}
		else{
			Map<String,Integer> newMap=new HashMap<String,Integer>();
			increment(newMap,key2,increase);
			map.put(key1,newMap);
		}
	}

	public static void increment(Map<String,Map<String,Integer>> map,String key1,String key2){
		increment(map,key1,key2,1);
	}

	public static <T extends Number> String toString(Map<String,T> map,boolean leadingSpace){
		StringBuilder b=new StringBuilder();
		for(Map.Entry<String,T> entry:map.entrySet()){
			b.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
		}
		if(leadingSpace){
			return b.toString();
		}
		else{
			return b.toString().trim();
		}
	}

	public static class MapComparator<V,M extends Comparable<M>> implements Comparator<V>{

		Map<V,M> map;

		public MapComparator(Map<V,M> map){
			this.map=map;
		}

		@Override
		public int compare(V a,V b){
			M ma=map.get(a);
			M mb=map.get(b);
			if(ma==null){
				if(mb==null){
					return 0;
				}
				else{
					return -1;
				}
			}
			else{
				if(mb==null){
					return 1;
				}
				else{
					return ma.compareTo(mb);
				}
			}
		}

	}

}
