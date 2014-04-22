package edu.cmu.cs.frank.ml.cluster.quality;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.LabelSet;
import edu.cmu.cs.frank.ml.classify.quality.LQMUtil;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.FormatUtil;

/**
 * Assign each instance in the cluster to be the most frequent true label in
 * that cluster, and calculate the accuracy of this assignment
 */

public class Purity implements ExternalCQM{
	
	@Override
	public double measure(List<Label> clustering,List<Label> truth){
		return measure(LQMUtil.makeConfusionMatrix(clustering,truth));
	}
	
	public double measure(int[][] cm){
		
		int correct=0;
		int total=0;
		for(int i=0;i<cm.length;i++){
			correct+=ArrayUtil.max(cm[i]);
			total+=ArrayUtil.sum(cm[i]);
		}
		
		return (double)correct/total;
		
	}
	
	/**
	 * For testing; example from http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
	 * Should output a value close to 0.71
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
		
		ExternalCQM m=new Purity();
		
		System.out.println(m.getClass().getSimpleName()+": "+FormatUtil.d4(m.measure(cLabels,tLabels)));
		
	}
	

}
