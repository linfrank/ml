package edu.cmu.cs.frank.ml.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.Classifier;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.Learner;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.FormatUtil;
import edu.cmu.cs.frank.util.MappedSets;


public class ClusterLearner implements Learner{
	
	static Logger log=Logger.getLogger(ClusterLearner.class);
	
	static final long serialVersionUID=20090312L;

	private Clusterer clusterer;
	private List<Label> clustering;

	public ClusterLearner(Clusterer clusterer){
		this.clusterer=clusterer;
	}
	
	public ClusterLearner(List<Label> clustering){
		this.clustering=clustering;
	}

	@Override
	public Classifier learn(Dataset dataset){
		
		if(clustering==null){
			clustering=clusterer.cluster(dataset);
		}
		
		MappedSets<Integer,Integer> clusters=new MappedSets<Integer,Integer>();
		for(int i=0;i<clustering.size();i++){
			clusters.add(clustering.get(i).getBestId(),i);
		}
		
		log.info("Clusters: "+clusters.keySet().size());
		
		int overwritten=0;
		
		Map<Instance,Label> map=new HashMap<Instance,Label>();
		for(int i=0;i<clusters.keySet().size();i++){
			Set<Integer> cluster=clusters.get(i);
			int[] votes=new int[dataset.getLabelSet().size()];
			for(int index:cluster){
				votes[dataset.getInstance(index).getLabel().getBestId()]++;
			}
			int most=ArrayUtil.maxIndex(votes);
			overwritten+=cluster.size()-votes[most];
			for(int index:cluster){
				Instance instance=dataset.getInstance(index);
				map.put(instance,dataset.getLabelSet().newLabel(most,instance.getLabel().getBestWeight()));
			}
		}
		log.info("Overwritten Labels: "+overwritten+" ("+FormatUtil.d2((double)overwritten/dataset.size())+")");
		
		return new ClusterClassifier(map);
	}

}
