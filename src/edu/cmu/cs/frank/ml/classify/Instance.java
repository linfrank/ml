package edu.cmu.cs.frank.ml.classify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A learning instance
 * 
 * @author Frank Lin
 */

public class Instance{

	String id;
	Label label;
	FeatureSet featureSet;
	Map<Integer,Double> features;
	double weight;

	public Instance(String id,Label label,FeatureSet featureSet,Map<Integer,Double> features,double weight){
		this.id=id;
		this.label=label;
		this.featureSet=featureSet;
		this.features=features;
		this.weight=weight;
	}
	
	public Instance(String id,FeatureSet featureSet){
		this(id,null,featureSet,new HashMap<Integer,Double>(),1.0);
	}

	public Instance(FeatureSet featureSet){
		this(null,featureSet);
	}

	public Instance(String id,Label label,FeatureSet featureSet,double[] featureValues,double weight){
		this(id,label,featureSet,new HashMap<Integer,Double>(),weight);
		for(int i=0;i<featureValues.length;i++){
			features.put(featureSet.index(String.valueOf(i)),featureValues[i]);
		}
	}
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id=id;
	}

	public Label getLabel(){
		return label;
	}

	public void setLabel(Label label){
		this.label=label;
	}

	public FeatureSet getFeatureSet(){
		return featureSet;
	}

	public void setFeatureSet(FeatureSet featureSet){
		this.featureSet=featureSet;
	}

	public Map<Integer,Double> getFeatures(){
		return features;
	}

	public String getFeatureName(int featureId){
		return featureSet.name(featureId);
	}

	public double getFeatureValue(int featureId){
		if(features.containsKey(featureId)){
			return features.get(featureId);
		}
		else{
			return 0.0;
		}
	}

	public double getFeatureValue(String name){
		if(featureSet.contains(name)){
			return getFeatureValue(featureSet.index(name));
		}
		else{
			return 0.0;
		}
	}

	public void setFeatures(Map<Integer,Double> features){
		this.features=features;
	}

	public void setFeature(String name,double value){
		if(value!=0.0){
			features.put(featureSet.index(name),value);
		}
	}
	
	public Set<String> getNonzeroFeatureNames(){
		Set<String> nonzero=new HashSet<String>();
		for(int index:features.keySet()){
			nonzero.add(featureSet.name(index));
		}
		return nonzero;
	}

	public void incrementFeature(int featureId){
		if(features.containsKey(featureId)){
			features.put(featureId,features.get(featureId)+1);
		}
		else{
			features.put(featureId,1.0);
		}
	}

	public void incrementFeature(String name){
		incrementFeature(featureSet.index(name));
	}

	public double getWeight(){
		return weight;
	}

	public void setWeight(double weight){
		this.weight=weight;
	}
	
	public Instance copy(){
		Instance copy=new Instance(featureSet);
		copy.id=id;
		copy.label=label.copy();
		copy.featureSet=featureSet;
		copy.features=new HashMap<Integer,Double>();
		for(Map.Entry<Integer,Double> entry:features.entrySet()){
			copy.features.put(new Integer(entry.getKey()),new Double(entry.getValue()));
		}
		copy.weight=weight;
		return copy;
	}

	public Instance shallowCopy(){
		return new Instance(id,label,featureSet,features,weight);
	}
	
	@Override
	public boolean equals(Object o){
		return id.equals(o);
	}

	@Override
	public int hashCode(){
		return id.hashCode();
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		if(id!=null){
			b.append("ID=").append(id).append(" ");
		}
		b.append("Label=").append(label).append(" W=").append(weight);
		for(Map.Entry<Integer,Double> entry:features.entrySet()){
			b.append(" ").append(featureSet.name(entry.getKey())).append("=").append(entry.getValue());
		}
		return b.toString();
	}


}
