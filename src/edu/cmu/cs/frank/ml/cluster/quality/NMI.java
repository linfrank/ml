package edu.cmu.cs.frank.ml.cluster.quality;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.LabelSet;
import edu.cmu.cs.frank.ml.classify.quality.LQMUtil;
import edu.cmu.cs.frank.util.FormatUtil;

/**
 * Normalized mutual information
 */

public class NMI implements ExternalCQM{
	
	@Override
	public double measure(List<Label> clustering,List<Label> truth){
		return measure(LQMUtil.makeConfusionMatrix(clustering,truth));
	}
	
	private double measure(int[][] cm){

		int n=0;
		int[] clusterCounts=new int[cm.length];
		int[] truthCounts=new int[cm[0].length];
		for(int i=0;i<cm.length;i++){
			for(int j=0;j<cm[i].length;j++){
				clusterCounts[i]+=cm[i][j];
				truthCounts[j]+=cm[i][j];
				n+=cm[i][j];
			}
		}

		// first calculate mutual information
		double mi=0.0;
		for(int i=0;i<cm.length;i++){
			for(int j=0;j<cm[i].length;j++){
				if(cm[i][j]!=0){
					mi+=(double)cm[i][j]/n*Math.log((double)n*cm[i][j]/(clusterCounts[i]*truthCounts[j]));
				}
			}
		}

		// then calculate entropies
		double ec=0.0;
		for(int i=0;i<clusterCounts.length;i++){
			double prob=(double)clusterCounts[i]/n;
			if(prob!=0.0){
				ec-=prob*Math.log(prob);
			}
		}
		double et=0.0;
		for(int i=0;i<truthCounts.length;i++){
			double prob=(double)truthCounts[i]/n;
			if(prob!=0.0){
				et-=prob*Math.log(prob);
			}
		}
		
		return mi/((ec+et)/2);

	}
	
	/**
	 * For testing; example from http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
	 * Should output a value close to 0.36
	 * 
	 * @param args none
	 */
	
	public static void main(String[] args){
		
		LabelSet cLabelSet=new LabelSet();
		LabelSet tLabelSet=new LabelSet();
		
		List<Label> cLabels=new ArrayList<Label>();
		List<Label> tLabels=new ArrayList<Label>();
		
		cLabels.add(cLabelSet.newLabel("1"));tLabels.add(tLabelSet.newLabel("x"));
		cLabels.add(cLabelSet.newLabel("1"));tLabels.add(tLabelSet.newLabel("x"));
		cLabels.add(cLabelSet.newLabel("1"));tLabels.add(tLabelSet.newLabel("x"));
		cLabels.add(cLabelSet.newLabel("1"));tLabels.add(tLabelSet.newLabel("x"));
		cLabels.add(cLabelSet.newLabel("1"));tLabels.add(tLabelSet.newLabel("x"));
		cLabels.add(cLabelSet.newLabel("1"));tLabels.add(tLabelSet.newLabel("o"));
		
		cLabels.add(cLabelSet.newLabel("2"));tLabels.add(tLabelSet.newLabel("x"));
		cLabels.add(cLabelSet.newLabel("2"));tLabels.add(tLabelSet.newLabel("o"));
		cLabels.add(cLabelSet.newLabel("2"));tLabels.add(tLabelSet.newLabel("o"));
		cLabels.add(cLabelSet.newLabel("2"));tLabels.add(tLabelSet.newLabel("o"));
		cLabels.add(cLabelSet.newLabel("2"));tLabels.add(tLabelSet.newLabel("o"));
		cLabels.add(cLabelSet.newLabel("2"));tLabels.add(tLabelSet.newLabel("d"));
		
		cLabels.add(cLabelSet.newLabel("3"));tLabels.add(tLabelSet.newLabel("x"));
		cLabels.add(cLabelSet.newLabel("3"));tLabels.add(tLabelSet.newLabel("x"));
		cLabels.add(cLabelSet.newLabel("3"));tLabels.add(tLabelSet.newLabel("d"));
		cLabels.add(cLabelSet.newLabel("3"));tLabels.add(tLabelSet.newLabel("d"));
		cLabels.add(cLabelSet.newLabel("3"));tLabels.add(tLabelSet.newLabel("d"));
		
		ExternalCQM m=new NMI();
		
		System.out.println(m.getClass().getSimpleName()+": "+FormatUtil.d4(m.measure(cLabels,tLabels)));
		
	}


}
