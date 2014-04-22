package edu.cmu.cs.frank.ml.classify.perceptron;

import java.util.Collection;

import edu.cmu.cs.frank.ml.classify.BinaryLabel;
import edu.cmu.cs.frank.ml.classify.Classifier;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.Label;

public class PerceptronClassifier implements Classifier{

	static final long serialVersionUID=20071115L;

	private Collection<Perceptron> perceptrons;

	public PerceptronClassifier(Collection<Perceptron> perceptrons){
		this.perceptrons=perceptrons;
	}

	@Override
	public Label classify(Instance instance){
		double vote=0.0;
		for(Perceptron perceptron:perceptrons){
			double prediction=perceptron.score(instance);
			if(prediction>0){
				vote+=perceptron.weight;
			}
			else{
				vote-=perceptron.weight;
			}
		}
		if(vote>0){
			return BinaryLabel.POS;
		}
		else{
			return BinaryLabel.NEG;
		}
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(super.toString()).append("\n");
		int i=0;
		for(Perceptron perceptron:perceptrons){
			b.append(i).append(" ").append(perceptron).append("\n");
			i++;
		}
		return b.toString();
	}

}
