package edu.cmu.cs.frank.ml.classify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A growable bidirectional map between strings and consecutive integers,
 * useful as a feature set or label set 
 * 
 * @author Frank Lin
 */

public class IndexedSet{
	
	static final long serialVersionUID=20090126L;

	private Map<String,Integer> nameIndexMap;
	private List<String> indexNameMap;

	public IndexedSet(){
		nameIndexMap=new HashMap<String,Integer>();
		indexNameMap=new ArrayList<String>();
	}

	public int index(String name){
		if(nameIndexMap.containsKey(name)){
			return nameIndexMap.get(name);
		}
		else{
			int index=indexNameMap.size();
			nameIndexMap.put(name,index);
			indexNameMap.add(name);
			return index;
		}
	}
	
	public String name(int index){
		if(contains(index)){
			return indexNameMap.get(index);
		}
		else{
			return null;
		}
	}

	public boolean contains(String name){
		return nameIndexMap.containsKey(name);
	}

	public boolean contains(int id){
		return id>-1&&id<indexNameMap.size();
	}

	public int size(){
		return indexNameMap.size();
	}

	@Override
	public String toString(){
		return indexNameMap.toString();
	}

}
