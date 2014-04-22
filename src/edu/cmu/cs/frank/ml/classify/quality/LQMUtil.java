package edu.cmu.cs.frank.ml.classify.quality;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.util.ArrayUtil;

public class LQMUtil{
	
	public static int[][] makeConfusionMatrix(List<Label> row,List<Label> column){
		int[][] cm=new int[row.get(0).getSet().size()][column.get(0).getSet().size()];
		for(int i=0;i<row.size();i++){
			cm[row.get(i).getBestId()][column.get(i).getBestId()]++;
		}
		return cm;
	}
	
	public static int[][] makeConfusionMatrix(int[] row,int[] column,int rowMax,int colMax){
		int[][] cm=new int[rowMax][colMax];
		for(int i=0;i<row.length;i++){
			cm[row[i]][column[i]]++;
		}
		return cm;
	}
	
	public static int[][] makeConfusionMatrix(int[] row,int[] column){
		return makeConfusionMatrix(row,column,ArrayUtil.max(row),ArrayUtil.max(column));
	}

}
