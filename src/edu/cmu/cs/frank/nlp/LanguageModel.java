package edu.cmu.cs.frank.nlp;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.frank.util.ScoreTable;


public class LanguageModel{
	
	private List<ScoreTable<String>> counts;
	private int[] numTokens;
	
	public LanguageModel(int order){
		counts=new ArrayList<ScoreTable<String>>(order);
		numTokens=new int[order];
		for(int i=0;i<order;i++){
			counts.add(new ScoreTable<String>());
			numTokens[i]=0;
		}
	}
	
	public LanguageModel(){
		this(1);
	}
	
	public void addNGram(int n,String gram){
		counts.get(n-1).increment(gram);
		numTokens[n-1]++;
	}
	
	public void addNGrams(String[] tokens){
		for(int i=0;i<tokens.length;i++){
			String gram=tokens[i];
			addNGram(1,gram);
			for(int j=1;j<numTokens.length;j++){
				if(i+j<tokens.length){
					gram=gram.concat(" ").concat(tokens[i+j]);
					addNGram(j+1,gram);
				}
			}
		}
	}
	
	public int getCount(int n,String gram){
		return (int)(double)counts.get(n-1).get(gram);
	}
	
	public double nGramProb(int n,String gram){
		return counts.get(n-1).get(gram)/numTokens[n-1];
	}
	
	public double entropy(int n){
		double entropy=0;
		for(double score:counts.get(n-1).scores()){
			entropy+=score*(score/numTokens[n-1]*Math.log(score/numTokens[n-1]));
		}
		return -entropy;
	}
	
	public double crossEntropy(int n,LanguageModel other){
		double cross=0;
		for(String gram:counts.get(n-1).keySet()){
			cross+=counts.get(n-1).get(gram)*(nGramProb(n,gram)*Math.log(other.nGramProb(n,gram)));
		}
		return -cross;
	}

	public static void main(String[] args){
		
		LanguageModel a=new LanguageModel();
		LanguageModel b=new LanguageModel();
		
		String[] aTokens=args[0].split("\\s+");
		String[] bTokens=args[1].split("\\s+");
		
		for(String token:aTokens){
			a.addNGram(1,token);
		}
		for(String token:bTokens){
			b.addNGram(1,token);
		}
		
		System.out.println("H(a)="+a.entropy(1));
		System.out.println("H(b)="+b.entropy(1));
		System.out.println("H(b,a)="+b.crossEntropy(1,a));
		
	}

}