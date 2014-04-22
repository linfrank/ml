package edu.cmu.cs.frank.ml.classify.quality;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;

/**
 * Macro-averaged recall:
 * (# correct labels per class) / (# true labels per class)
 */

public class Recall implements LabelQualityMeasure{
	
	private Precision prec=new Precision();
	
	@Override
	public double measure(List<Label> pred,List<Label> truth){
		return measure(LQMUtil.makeConfusionMatrix(pred,truth));
	}
	
	@Override
	public double measure(int[][] cm){
		// recall is precision with transposed confusion matrix
		int[][] tcm=new int[cm[0].length][cm.length];
		for(int i=0;i<cm.length;i++){
			for(int j=0;j<cm[0].length;j++){
				tcm[j][i]=cm[i][j];
			}
		}
		return prec.measure(tcm);
	}

}
