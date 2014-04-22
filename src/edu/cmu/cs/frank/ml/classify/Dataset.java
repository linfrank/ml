package edu.cmu.cs.frank.ml.classify;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

public class Dataset implements Iterable<Instance>,Serializable{

	static Logger log=Logger.getLogger(Dataset.class);

	static final long serialVersionUID=20071112L;

	private LabelSet labelSet;
	private FeatureSet featureSet;
	private List<Instance> instanceList;
	private Map<String,Instance> instanceMap;

	public Dataset(LabelSet labelSet,FeatureSet featureSet){
		this.labelSet=labelSet;
		this.featureSet=featureSet;
		instanceList=new ArrayList<Instance>();
		instanceMap=new HashMap<String,Instance>();
	}
	
	public Dataset(LabelSet labelSet){
		this(labelSet,new FeatureSet());
	}

	public Dataset(FeatureSet featureSet){
		this(new LabelSet(),featureSet);
	}

	public Dataset(){
		this(new LabelSet(),new FeatureSet());
	}
	
	public Dataset(Collection<Instance> instances){
		this();
		if(instances.size()>0){
			Instance first=instances.iterator().next();
			labelSet=first.label.set;
			featureSet=first.featureSet;
			add(instances);
		}
	}

	@Override
	public Iterator<Instance> iterator(){
		return instanceList.iterator();
	}
	
	public void setLabelSet(LabelSet labelSet){
		this.labelSet=labelSet;
	}
	
	public LabelSet getLabelSet(){
		return labelSet;
	}

	public FeatureSet getFeatureSet(){
		return featureSet;
	}

	public void add(Instance instance){
		if(instance.featureSet!=featureSet){
			log.error("Instance feature set incompatible with dataset: "+instance);
		}
		else if(instance.label!=null&&instance.label.set!=labelSet){
			log.error("Instance label set incompatible with dataset: "+instance);
		}
		else if(instance.id!=null&&instanceMap.containsKey(instance.id)){
			log.warn("ID already exists, instance not added: "+instance);
		}
		else{
			instanceList.add(instance);
			if(instance.id!=null){
				instanceMap.put(instance.id,instance);
			}
		}
	}

	public void add(Collection<Instance> instances){
		for(Instance instance:instances){
			add(instance);
		}
	}
	
	public Label newLabel(String id){
		return labelSet.newLabel(id);
	}

	public Instance newInstance(String id){
		Instance instance=new Instance(id,featureSet);
		add(instance);
		return instance;
	}

	public Instance newInstance(){
		return newInstance(null);
	}

	public List<Instance> getInstances(){
		return instanceList;
	}

	public Map<String,Instance> getMappedInstances(){
		return instanceMap;
	}

	public Instance getInstance(int index){
		return instanceList.get(index);
	}

	public Instance getInstance(String id){
		return instanceMap.get(id);
	}

	public boolean contains(String id){
		return instanceMap.containsKey(id);
	}

	public int findInstanceIndex(String id){
		return instanceList.indexOf(instanceMap.get(id));
	}
	
	public Dataset copy(){
		Dataset copy=new Dataset(labelSet,featureSet);
		copy.add(instanceList);
		return copy;
	}

	public List<Label> getLabels(){
		List<Label> labels=new ArrayList<Label>(instanceList.size());
		for(Instance instance:instanceList){
			labels.add(instance.getLabel());
		}
		return labels;
	}

	public Map<String,Label> getMappedLabels(){
		Map<String,Label> map=new HashMap<String,Label>();
		for(Instance instance:instanceList){
			map.put(instance.id,instance.label);
		}
		return map;
	}

	public void shuffle(Random random){
		Collections.shuffle(instanceList,random);
	}

	public void shuffle(){
		Collections.shuffle(instanceList);
	}

	public int size(){
		return instanceList.size();
	}

	public Map<String,Integer> getLabelCounts(){
		int[] counts=new int[labelSet.size()];
		for(Instance instance:instanceList){
			if(instance.label!=null){
				counts[instance.label.getBestId()]++;
			}
		}
		Map<String,Integer> map=new HashMap<String,Integer>();
		for(int i=0;i<counts.length;i++){
			map.put(labelSet.name(i),counts[i]);
		}
		return map;
	}

	public Map<String,Double> getLabelDistribution(){
		Map<String,Integer> counts=getLabelCounts();
		double total=0.0;
		for(String name:counts.keySet()){
			total+=counts.get(name);
		}
		Map<String,Double> dist=new HashMap<String,Double>();
		for(String name:counts.keySet()){
			dist.put(name,counts.get(name)/total);
		}
		return dist;
	}

	public String getStat(){
		StringBuilder b=new StringBuilder();
		b.append(size()).append(" instances").append("\n");
		b.append(featureSet.size()).append(" features").append("\n");
		b.append(labelSet.size()).append(" labels").append("\n");
		b.append("Label Set: ").append(labelSet).append("\n");
		b.append("Label Counts: ").append(getLabelCounts()).append("\n");
		b.append("Label Distribution: ").append(getLabelDistribution()).append("\n");
		return b.toString();
	}

	public Dataset subset(int size){
		if(size<=instanceList.size()){
			Dataset subset=new Dataset(labelSet,featureSet);
			for(Instance instance:instanceList){
				subset.add(instance);
			}
			return subset;
		}
		else{
			log.error("Subset size greater than full set size: "+size+" > "+instanceList.size());
			return null;
		}
	}
	
	public Dataset subsetPerClass(int perClass,boolean random){
		Dataset subset=new Dataset(labelSet,featureSet);
		List<Instance> instances;
		if(random){
			instances=new ArrayList<Instance>(instanceList);
			Collections.shuffle(instances);
		}
		else{
			instances=instanceList;
		}
		int[] counts=new int[labelSet.size()];
		int enough=0;
		for(int i=0;i<instances.size()&&enough<counts.length;i++){
			Instance instance=instances.get(i);
			int bestLabelId=instance.getLabel().getBestId();
			if(counts[bestLabelId]<perClass){
				subset.add(instance);
				counts[bestLabelId]++;
				if(counts[bestLabelId]>=perClass){
					enough++;
				}
			}
		}
		return subset;
	}

	public List<Dataset> split(double fraction){
		int numFirst=(int)(fraction*instanceList.size());
		Dataset first=new Dataset(labelSet,featureSet);
		for(int i=0;i<numFirst;i++){
			first.add(instanceList.get(i));
		}
		Dataset second=new Dataset(labelSet,featureSet);
		for(int i=numFirst;i<instanceList.size();i++){
			second.add(instanceList.get(i));
		}
		List<Dataset> split=new ArrayList<Dataset>(2);
		split.add(first);
		split.add(second);
		return split;
	}

	public List<Dataset> split(int numFolds){
		List<Dataset> split=new ArrayList<Dataset>(numFolds);
		int foldSize=instanceList.size()/numFolds;
		for(int i=0;i<numFolds;i++){
			int start=i*foldSize;
			int end;
			if(i<numFolds-1){
				end=(i+1)*foldSize;
			}
			else{
				end=instanceList.size();
			}
			Dataset subset=new Dataset(labelSet,featureSet);
			subset.add(instanceList.subList(start,end));
			split.add(subset);
		}
		return split;
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append("Label Set: ").append(labelSet).append("\n");
		b.append("Feature Set: ").append(featureSet).append("\n");
		b.append("Instances:").append("\n");
		for(Instance instance:instanceList){
			b.append(instance).append("\n");
		}
		return b.toString();
	}

}
