package edu.cmu.cs.frank.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An object for storing statistics; can be recursively nested
 * 
 * @authoer Frank Lin
 */

public class Stat{

	private String name;
	private LinkedHashMap<String,Object> map;

	public Stat(String name){
		this.name=name;
		map=new LinkedHashMap<String,Object>();
	}

	public Stat(){
		this(null);
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name=name;
	}

	public Object get(String key){
		return map.get(key);
	}
	
	public Stat getStat(String key){
		Object value=map.get(key);
		if(value instanceof Stat){
			return (Stat)value;
		}
		else{
			return null;
		}
	}

	public void add(String key,Object value){
		if(value instanceof Stat){
			Stat nested=(Stat)value;
			nested.setName(key);
		}
		map.put(key,value);
	}

	@Override
	public String toString(){
		boolean emptyLine=false;
		StringBuilder b=new StringBuilder();
		if(name!=null){
			b.append(name).append(":\n");
		}
		for(Map.Entry<String,Object> entry:map.entrySet()){
			if(entry.getValue() instanceof Stat){
				if(!emptyLine){
					b.append("\n");
				}
				b.append(entry.getValue());
				b.append("\n");
				emptyLine=true;
			}
			else{
				b.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
				emptyLine=false;
			}
		}
		return b.toString();
	}

}
