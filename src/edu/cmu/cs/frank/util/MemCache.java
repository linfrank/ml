package edu.cmu.cs.frank.util;

import java.util.HashMap;
import java.util.Map;


public class MemCache{
	
	private static final Map<String,Object> cache=new HashMap<String,Object>();
	
	public static boolean contains(String key){
		return cache.containsKey(key);
	}
	
	public static Object get(String key){
		return cache.get(key);
	}
	
	public static void cache(String key,Object data){
		cache.put(key,data);
	}

}
