package edu.cmu.cs.frank.ml.cluster.quality;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.quality.LQMUtil;
import edu.cmu.cs.frank.util.ArrayUtil;


public class Hungarian implements ExternalCQM{
	
	@Override
	public double measure(List<Label> clustering,List<Label> truth){
		return measure(LQMUtil.makeConfusionMatrix(clustering,truth));
	}
	
	private int[] assign(int[][] cm){
		
		// change it to a cost minimization problem (instead of maximization)
		int max=Integer.MIN_VALUE;
		for(int i=0;i<cm.length;i++){
			for(int j=0;j<cm[i].length;j++){
				if(cm[i][j]>max){
					max=cm[i][j];
				}
			}
		}
		for(int i=0;i<cm.length;i++){
			for(int j=0;j<cm[i].length;j++){
				cm[i][j]=max-cm[i][j];
			}
		}
		
		// subtract lowest value from each row
		for(int i=0;i<cm.length;i++){
			int lowest=ArrayUtil.min(cm[i]);
			for(int j=0;j<cm.length;j++){
				cm[i][j]=cm[i][j]-lowest;
			}
		}
		
		// subtract lowest value from each column
		for(int i=0;i<cm.length;i++){
			int lowest=ArrayUtil.min(cm[i]);
			for(int j=0;j<cm.length;j++){
				cm[i][j]=cm[i][j]-lowest;
			}
		}
		
		// halfway through, needs to finish up
		
		int[] a=new int[cm.length];
		
		return a;
		
	}
	
	private double measure(int[][] cm){
		
		int[] a=assign(ArrayUtil.copy(cm));
		
		int correct=0;
		int total=0;
		for(int i=0;i<a.length;i++){
			if(a[i]>=0){
				correct+=cm[i][a[i]];
			}
			total+=ArrayUtil.sum(cm[i]);
		}
		
		return (double)correct/total;
		
	}

}
