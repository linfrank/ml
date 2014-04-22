package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.HashSet;
import java.util.Set;

public class IdSeeder implements Seeder{
	
	protected Set<? extends Object> idSet;
	
	public IdSeeder(Set<? extends Object> idSet){
		this.idSet=idSet;
	}
	
	@Override
	public Set<LPVertex> seed(LPGraph g){
		Set<LPVertex> seeds=new HashSet<LPVertex>();
		for(LPVertex v:g.getVertices()){
			if(idSet.contains(v.id)){
				seeds.add(v);
			}
		}
		return seeds;
	}
	
	@Override
	public String toString(){
		return "IdSeeder ("+idSet.size()+" ID's)";
	}

}
