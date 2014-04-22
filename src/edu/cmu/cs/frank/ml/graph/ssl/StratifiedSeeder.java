package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

public class StratifiedSeeder implements Seeder{

	public static Logger log=Logger.getLogger(StratifiedSeeder.class);

	private Random random;
	private double r;

	public StratifiedSeeder(double r,Random random){
		this.r=r;
		this.random=random;
	}

	public StratifiedSeeder(double r){
		this(r,new Random());
	}
	
	private List<List<LPVertex>> stratify(LPGraph graph){
		List<List<LPVertex>> stratified=new ArrayList<List<LPVertex>>(graph.getDimensions());
		for(int i=0;i<graph.getDimensions();i++){
			stratified.add(new ArrayList<LPVertex>());
		}
		for(LPVertex vertex:graph.getVertices()){
			int label=vertex.trueLabel;
			stratified.get(label).add(vertex);
		}
		return stratified;
	}

	@Override
	public Set<LPVertex> seed(LPGraph graph){

		Set<LPVertex> seeds=new HashSet<LPVertex>();

		List<List<LPVertex>> stratified=stratify(graph);
		
		for(List<LPVertex> list:stratified){
			int n=(int)(r*list.size());
			if(n<1){
				n=1;
			}
			Collections.shuffle(list,random);
			for(int i=0;i<n;i++){
				seeds.add(list.get(i));
			}
		}

		return seeds;

	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(this.getClass().getSimpleName()).append(": R=").append(r);
		return b.toString();
	}

}
