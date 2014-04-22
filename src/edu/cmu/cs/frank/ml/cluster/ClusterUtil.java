package edu.cmu.cs.frank.ml.cluster;

import java.util.ArrayList;
import java.util.List;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.LabelSet;


public class ClusterUtil{
	
	public static List<Label> toLabels(DoubleMatrix2D scores){
		LabelSet labelSet=new LabelSet(scores.columns());
		List<Label> labels=new ArrayList<Label>(scores.rows());
		for(int i=0;i<scores.rows();i++){
			labels.add(labelSet.newLabel(scores.viewRow(i).toArray()));
		}
		return labels;
	}
	
	public static int[] toIndexArray(List<Label> labels){
		int[] a=new int[labels.size()];
		for(int i=0;i<labels.size();i++){
			if(labels.get(i)!=null){
				a[i]=labels.get(i).getBestId();
			}
			else{
				a[i]=-1;
			}
		}
		return a;
	}

}
