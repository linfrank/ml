package edu.cmu.cs.frank.ml.cluster.quality;

import java.util.List;
import java.util.Map;

import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.Label;

public class NCut implements InternalCQM{

	@Override
	public double measure(List<Label> clustering,Dataset dataset){
		
		int numClusters=clustering.get(0).getSet().size();
		
		double[] total=new double[numClusters];
		double[] cut=new double[numClusters];

		for(int i=0;i<dataset.size();i++){
			Instance instance=dataset.getInstance(i);
			for(Map.Entry<Integer,Double> entry:instance.getFeatures().entrySet()){
				int clusterId=clustering.get(i).getBestId();
				total[clusterId]+=entry.getValue();
				if(clusterId!=clustering.get(entry.getKey()).getBestId()){
					cut[clusterId]+=entry.getValue();
				}
			}
		}
		
		double nCut=0.0;
		for(int i=0;i<numClusters;i++){
			if(total[i]>0){
				nCut+=cut[i]/total[i];
			}
		}
		return nCut;
		
	}
	
}
