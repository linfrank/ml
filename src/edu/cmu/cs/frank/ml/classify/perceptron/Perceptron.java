package edu.cmu.cs.frank.ml.classify.perceptron;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.util.ArrayUtil;

public class Perceptron implements Comparable<Perceptron>{
	
	public double[] vector;
	public double weight;
	
	public Perceptron(double[] vector,double weight){
		this.vector=vector;
		this.weight=weight;
	}
	
	public Perceptron(int dimension){
		vector=new double[dimension];
		weight=0.0;
	}
	
	public double score(Instance instance){
		double prediction=0.0;
		for(Map.Entry<Integer,Double> feature:instance.getFeatures().entrySet()){
			prediction+=vector[feature.getKey()]*feature.getValue();
		}
		return prediction;
	}
	
	public Perceptron update(Instance instance,int truth){
		double[] updated=ArrayUtil.copy(vector);
		for(Map.Entry<Integer,Double> feature:instance.getFeatures().entrySet()){
			updated[feature.getKey()]+=feature.getValue()*truth;
		}
		return new Perceptron(updated,1.0);
	}
	
	@Override
	public int compareTo(Perceptron other){
		double diff=this.weight-other.weight;
		if(diff>0){
			return 1;
		}
		else if(diff<0){
			return -1;
		}
		else{
			return 0;
		}
	}
	
	public void add(Perceptron other){
		for(int i=0;i<vector.length;i++){
			vector[i]+=other.vector[i]*other.weight;
		}
	}
	
	public void add(Collection<Perceptron> others){
		for(Perceptron other:others){
			add(other);
		}
	}
	
	public static Perceptron average(int dimension,Collection<Perceptron> perceptrons){
		Perceptron averaged=new Perceptron(dimension);
		averaged.add(perceptrons);
		return averaged;
	}
	
	public static Perceptron average(int dimension,Collection<Perceptron> perceptrons,double weight){
		Perceptron averaged=average(dimension,perceptrons);
		averaged.weight=weight;
		return averaged;
	}
	
	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append("w=").append(weight).append(" ").append(Arrays.toString(vector));
		return b.toString();
	}

}
