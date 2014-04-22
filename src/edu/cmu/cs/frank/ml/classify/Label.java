package edu.cmu.cs.frank.ml.classify;

import java.io.Serializable;

import edu.cmu.cs.frank.util.ArrayUtil;

public class Label implements Serializable{
	
	static final long serialVersionUID=20071111L;
	
	final LabelSet set;
	double[] weights;
	
	Label(LabelSet set,double[] weights){
		this.set=set;
		this.weights=weights;
	}
	
	Label(LabelSet set){
		this(set,new double[set.size()]);
	}
	
	Label(LabelSet set,int id,double weight){
		this(set);
		weights[id]=weight;
	}
	
	Label(LabelSet set,int id){
		this(set,id,1.0);
	}
	
	public LabelSet getSet(){
		return set;
	}
	
	public void set(String name,double weight){
		int id=set.index(name);
		if(id>=weights.length){
			double[] newWeights=new double[set.size()];
			for(int i=0;i<weights.length;i++){
				newWeights[i]=weights[i];
			}
			weights=newWeights;
		}
		weights[id]=weight;
	}
	
	public void setWeights(double[] weights){
		this.weights=weights;
	}
	
	public int getBestId(){
		return ArrayUtil.maxIndex(weights);
	}
	
	public String getBestName(){
		return set.name(getBestId());
	}
	
	public double getWeight(String name){
		int id=set.index(name);
		if(id<weights.length){
			return weights[id];
		}
		else{
			return 0.0;
		}
	}
	
	public double getBestWeight(){
		return ArrayUtil.max(weights);
	}

	@Override
	public boolean equals(Object o){
		if(this==o){
			return true;
		}
		else if(o instanceof Label){
			Label other=(Label)o;
			return this.getBestName().equals(other.getBestName());
		}
		else{
			return false;
		}
	}
	
	public Label copy(){
		Label copy=new Label(set);
		copy.weights=ArrayUtil.copy(weights);
		return copy;
	}
	
	@Override
	public String toString(){
		return getBestName();
	}

}
