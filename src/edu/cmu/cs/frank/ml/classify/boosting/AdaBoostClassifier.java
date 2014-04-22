package edu.cmu.cs.frank.ml.classify.boosting;

import java.util.List;

import edu.cmu.cs.frank.ml.classify.BinaryLabel;
import edu.cmu.cs.frank.ml.classify.Classifier;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.Label;

public class AdaBoostClassifier implements Classifier{
	
	static final long serialVersionUID=20071113L;
	
	private List<Double> alphas;
	private List<Classifier> baseClassifiers;
	
	public AdaBoostClassifier(List<Double> alphas,List<Classifier> baseClassifiers){
		this.alphas=alphas;
		this.baseClassifiers=baseClassifiers;
	}
	
	@Override
	public Label classify(Instance instance){
		double weighted=classifyWeighted(instance);
		if(weighted>0){
			return BinaryLabel.POS;
		}
		else{
			return BinaryLabel.NEG;
		}
	}
	
	public double classifyWeighted(Instance instance){
		double sum=0;
		for(int i=0;i<alphas.size();i++){
			sum+=alphas.get(i)*((BinaryLabel)baseClassifiers.get(i).classify(instance)).getPolarValue();
		}
		return sum;
	}
	
	public List<Double> getAlphas(){
		return alphas;
	}

	public void setAlphas(List<Double> alphas){
		this.alphas=alphas;
	}

	public List<Classifier> getBaseClassifiers(){
		return baseClassifiers;
	}

	public void setBaseClassifiers(List<Classifier> baseClassifiers){
		this.baseClassifiers=baseClassifiers;
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(this.getClass().getSimpleName()).append(":\n");
		for(int i=0;i<alphas.size();i++){
			b.append("t=").append(i).append(" Alpha: ").append(alphas.get(i)).append("\n");
			b.append("t=").append(i).append(" Base Classifier: ").append(baseClassifiers.get(i)).append("\n");
		}
		return b.toString();
	}

}
