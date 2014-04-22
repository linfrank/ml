package edu.cmu.cs.frank.ml.cluster.quality;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.LabelSet;
import edu.cmu.cs.frank.ml.cluster.ClusterUtil;
import edu.cmu.cs.frank.util.FormatUtil;

/**
 * Rand index - quite expensive - n^2 time
 */

public class RI implements ExternalCQM{
	
	@Override
	public double measure(List<Label> clustering,List<Label> truth){
		return measure(ClusterUtil.toIndexArray(clustering),ClusterUtil.toIndexArray(truth));
	}
	
	private double measure(int[] c,int[] t){
		
		int n=c.length;
		
		int correct=0;
		for(int i=0;i<n;i++){
			for(int j=0;j<n;j++){
				if((c[i]==c[j]&&t[i]==t[j])||(c[i]!=c[j]&&t[i]!=t[j])){
					correct++;
				}
			}
		}
		
		return (double)correct/(n*n);
		
	}
	
	/**
	 * For testing; example from http://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-clustering-1.html
	 * Should output a value close to 0.68
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
		
		ExternalCQM m=new RI();
		
		System.out.println(m.getClass().getSimpleName()+": "+FormatUtil.d4(m.measure(cLabels,tLabels)));
		
	}


}
