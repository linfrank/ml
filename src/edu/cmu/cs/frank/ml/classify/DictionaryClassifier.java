package edu.cmu.cs.frank.ml.classify;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.frank.nlp.PorterStemmer;
import edu.cmu.cs.frank.util.IOUtil;

/**
 * According to the postive/negative words classifier described in:
 * 
 * author="Anubhav Kale and Amit Karandikar and Pranam Kolari and Akshay Java and Tim Finin and Anupam Joshi",
 * title="Modeling Trust and Influence in the Blogosphere Using Link Polarity",
 * booktitle="Proceedings of the International Conference on Weblogs and Social Media (ICWSM 2007)",
 * year={2007}
 * 
 */

public class DictionaryClassifier implements Classifier{
	
	static final long serialVersionUID=20071115L;
	
	private PorterStemmer stemmer;
	private Set<String> posWords;
	private Set<String> negWords;

	public DictionaryClassifier(String posFile,String negFile){
		stemmer=new PorterStemmer();
		posWords=loadWordFile(posFile);
		negWords=loadWordFile(negFile);
		for(String word:negWords){
			posWords.add("not ".concat(word));
		}
		for(String word:posWords){
			negWords.add("not ".concat(word));
		}
	}
	
	private String normalize(String word){
		String norm=word.trim().toLowerCase();
		if(norm.indexOf('_')==-1){
			norm=stemmer.stem(norm);
		}
		else{
			norm=norm.replace('_',' ');
		}
		return norm;
	}
	
	private Set<String> loadWordFile(String fileName){
		Set<String> words=new HashSet<String>();
		try{
			List<String> lines=IOUtil.readLines(fileName);
			for(String line:lines){
				words.add(normalize(line));
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return words;
	}

	@Override
	public Label classify(Instance instance){
		int nPos=0;
		int nNeg=0;
		for(Map.Entry<Integer,Double> feature:instance.getFeatures().entrySet()){
			String word=instance.getFeatureName(feature.getKey());
			word=normalize(word);
			if(posWords.contains(word)){
				nPos+=(int)feature.getValue().doubleValue();
			}
			else if(negWords.contains(word)){
				nNeg+=(int)feature.getValue().doubleValue();
			}
		}
		int polarity;
		if(nPos+nNeg==0){
			polarity=0;
		}
		else{
			polarity=(nPos-nNeg)/(nPos+nNeg);
		}
		if(polarity<0){
			return BinaryLabel.NEG;
		}
		else{
			return BinaryLabel.POS;
		}
	}
	
}
