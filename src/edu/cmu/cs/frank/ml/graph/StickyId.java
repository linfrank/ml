package edu.cmu.cs.frank.ml.graph;

import java.util.Map;
import java.util.TreeMap;

public class StickyId extends Id{

	private TreeMap<Object,Object> stuck;

	public StickyId(Object id){
		super(id);
	}

	public void stick(Object key,Object value){
		if(stuck==null){
			stuck=new TreeMap<Object,Object>();
		}
		stuck.put(key,value);
	}

	public Object getStuck(Object key){
		if(stuck==null){
			return null;
		}
		else{
			return stuck.get(key);
		}
	}

	public int numStuck(){
		if(stuck==null){
			return 0;
		}
		else{
			return stuck.size();
		}
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(super.toString());
		if(stuck!=null){
			for(Map.Entry<Object,Object> entry:stuck.entrySet()){
				b.append(" [").append(entry.getKey()).append(": ").append(entry.getValue()).append("]");
			}
		}
		return b.toString();
	}

}
