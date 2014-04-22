package edu.cmu.cs.frank.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MappedSets<K,V>{
	
	private Map<K,Set<V>> map=new HashMap<K,Set<V>>();
	
	public void add(K key,V item){
		if(!map.containsKey(key)){
			map.put(key,new HashSet<V>());
		}
		map.get(key).add(item);
	}
	
	public Set<K> keySet(){
		return map.keySet();
	}
	
	public Set<V> get(K key){
		return map.get(key);
	}

}
