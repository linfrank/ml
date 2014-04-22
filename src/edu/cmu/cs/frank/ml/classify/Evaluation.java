package edu.cmu.cs.frank.ml.classify;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.frank.ml.classify.quality.LQMUtil;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.ScoreTable;
import edu.cmu.cs.frank.util.TextUtil;

/**
 * @author Frank Lin
 */

public class Evaluation implements Serializable{

	static final long serialVersionUID=20080630L;

	private static final DecimalFormat doubleFormat=new DecimalFormat("0.00000");

	transient public int total;
	transient public int incorrect;

	transient public double errorRate;
	transient public double accuracy;
	transient public double precision;
	transient public double recall;
	transient public double f1;

	transient public int[][] confusionMatrix;
	transient public double[] precisions;
	transient public double[] recalls;
	transient public double[] f1s;
	
	transient private String[] paddedNames;

	private LabelSet labelSet;
	private List<Label> truth;
	private List<Label> prediction;
	private Map<String,Integer> idMap;
	
	private ScoreTable<String> custom;
	
	private List<Evaluation> averaging;
	private boolean averaged;

	public Evaluation(LabelSet labelSet,List<Label> truth,List<Label> prediction,Map<String,Integer> idMap){
		this.labelSet=labelSet;
		this.truth=truth;
		this.prediction=prediction;
		this.idMap=idMap;
		averaging=null;
		averaged=false;
		initialize();
		calculateSingle();
	}
	
	public Evaluation(LabelSet labelSet,List<Label> truth,List<Label> prediction){
		this(labelSet,truth,prediction,new HashMap<String,Integer>());
	}
	
	public Evaluation(LabelSet labelSet,List<Evaluation> averaging){
		this.labelSet=labelSet;
		this.averaging=averaging;
		averaged=true;
		initialize();
		calculateAverage();
	}
	
	private void initialize(){
		custom=new ScoreTable<String>();
		precisions=new double[labelSet.size()];
		recalls=new double[labelSet.size()];
		f1s=new double[labelSet.size()];
		paddedNames=getPaddedLabelNames(labelSet);
	}

	public void setCustom(String name,double value){
		custom.set(name,value);
	}

	public List<String> getCustomNames(){
		return custom.keyList();
	}
	
	public double getCustom(String name){
		return custom.get(name);
	}

	public LabelSet getLabelSet(){
		return labelSet;
	}

	public List<Label> getTruth(){
		return truth;
	}

	public List<Label> getPrediction(){
		return prediction;
	}

	public void setIdMap(Map<String,Integer> idMap){
		this.idMap=idMap;
	}

	public Map<String,Integer> getIdMap(){
		return idMap;
	}

	public Set<String> getIds(){
		return idMap.keySet();
	}

	public Label getTruth(String id){
		return truth.get(idMap.get(id));
	}
	
	public Label getPrediction(String id){
		return prediction.get(idMap.get(id));
	}
	
	public void calculateSingle(){
		total=truth.size();
		incorrect=0;
		confusionMatrix=LQMUtil.makeConfusionMatrix(truth,prediction);
		for(int i=0;i<confusionMatrix.length;i++){
			int perClassCorrect=confusionMatrix[i][i];
			int perClassPredicted=ArrayUtil.sumColumn(confusionMatrix,i);
			int perClassTrue=ArrayUtil.sumRow(confusionMatrix,i);
			incorrect+=perClassPredicted-perClassCorrect;
			precisions[i]=(double)perClassCorrect/perClassPredicted;
			precisions[i]=fix(precisions[i]);
			recalls[i]=(double)perClassCorrect/perClassTrue;
			recalls[i]=fix(recalls[i]);
			f1s[i]=2*precisions[i]*recalls[i]/(precisions[i]+recalls[i]);
			f1s[i]=fix(f1s[i]);
		}
		errorRate=((double)incorrect)/total;
		accuracy=1.0-errorRate;
		precision=ArrayUtil.average(precisions);
		recall=ArrayUtil.average(recalls);
		f1=ArrayUtil.average(f1s);
	}
	
	public void calculateAverage(){
		// sum up
		for(Evaluation eval:averaging){
			total+=eval.total;
			incorrect+=eval.incorrect;
			errorRate+=eval.errorRate;
			accuracy+=eval.accuracy;
			precision+=eval.precision;
			recall+=eval.recall;
			f1+=eval.f1;
			confusionMatrix=new int[labelSet.size()][labelSet.size()];
			for(int i=0;i<confusionMatrix.length;i++){
				for(int j=0;j<confusionMatrix[i].length;j++){
					confusionMatrix[i][j]+=eval.confusionMatrix[i][j];
				}
			}
			for(int i=0;i<labelSet.size();i++){
				precisions[i]+=eval.precisions[i];
				recalls[i]+=eval.recalls[i];
				f1s[i]+=eval.f1s[i];
			}
			custom.mergeAdd(eval.custom);
		}

		// divide by number of evals
		errorRate/=averaging.size();
		accuracy/=averaging.size();
		precision/=averaging.size();
		recall/=averaging.size();
		f1/=averaging.size();
		for(int i=0;i<labelSet.size();i++){
			precisions[i]/=averaging.size();
			recalls[i]/=averaging.size();
			f1s[i]/=averaging.size();
		}
		for(String key:custom.keySet()){
			custom.set(key,custom.get(key)/averaging.size());
		}
		
	}

	public static Evaluation calculate(LabelSet labelSet,List<Label> truth,List<Label> prediction,Map<String,Integer> idMap){
		return new Evaluation(labelSet,truth,prediction,idMap);
	}
	
	public static Evaluation calculate(LabelSet labelSet,List<Label> truth,List<Label> prediction){
		return new Evaluation(labelSet,truth,prediction);
	}

	public static Evaluation average(List<Evaluation> evals){
		if(evals.size()<1){
			return null;
		}
		if(evals.size()==1){
			return evals.get(0);
		}
		else{
			return new Evaluation(evals.get(0).labelSet,evals);
		}
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append("Total Instances: ").append(total).append("\n");
		b.append("Misclassified Instances: ").append(incorrect).append("\n");
		b.append("Error Rate: ").append(doubleFormat.format(errorRate)).append("\n");
		b.append("Accuracy: ").append(doubleFormat.format(accuracy)).append("\n");
		b.append("Precision (Macro-Averaged): ").append(doubleFormat.format(precision)).append("\n");
		b.append("Recall (Macro-Averaged): ").append(doubleFormat.format(recall)).append("\n");
		b.append("F1 (Macro-Averaged): ").append(doubleFormat.format(f1)).append("\n");
		b.append("Per-Class:").append("\n");
		for(int i=0;i<labelSet.size();i++){
			b.append(" ").append(paddedNames[i]);
			b.append(" Prec=").append(doubleFormat.format(precisions[i]));
			b.append(" Recall=").append(doubleFormat.format(recalls[i]));
			b.append(" F1=").append(doubleFormat.format(f1s[i]));
			b.append("\n");
		}
		b.append("Confusion Matrix (Vertical=True, Horizontal=Predicted):").append("\n");
		b.append(ArrayUtil.toTable(confusionMatrix));
		b.append("Matrix Legend: ").append(labelSet).append("\n");
		if(custom.size()>0){
			b.append("Other:").append("\n");
			List<String> customKeys=new ArrayList<String>(custom.keySet());
			Collections.sort(customKeys);
			for(String customKey:customKeys){
				b.append(" ").append(customKey).append("=").append(custom.get(customKey)).append("\n");
			}
		}
		if(averaged){
			b.append("[Averaged over ").append(averaging.size()).append(" trials]");
		}
		return b.toString();
	}

	private static double fix(double a){
		if(Double.isInfinite(a)||Double.isNaN(a)){
			return 0.0;
		}
		else{
			return a;
		}
	}

	private static String[] getPaddedLabelNames(LabelSet labelSet){
		int maxLength=0;
		for(int i=0;i<labelSet.size();i++){
			int length=labelSet.name(i).length();
			if(length>maxLength){
				maxLength=length;
			}
		}
		String[] padded=new String[labelSet.size()];
		for(int i=0;i<padded.length;i++){
			padded[i]=TextUtil.padRight(labelSet.name(i),' ',maxLength);
		}
		return padded;
	}
	
	private void writeObject(ObjectOutputStream out)throws IOException{
		out.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream in)throws IOException,ClassNotFoundException{
		in.defaultReadObject();
		initialize();
		if(averaged){
			calculateAverage();
		}
		else{
			calculateSingle();
		}
	}

}
