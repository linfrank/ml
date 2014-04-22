package edu.cmu.cs.frank.ml.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.LabelSet;
import edu.cmu.cs.frank.util.Parameters;

public class RandomClusterer implements Clusterer{

	protected static Logger log=Logger.getLogger(RandomClusterer.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20080512L;

		public int k=2;

	}

	private Params p;

	@Override
	public void setParams(Parameters params){
		this.p=(Params)params;
	}

	@Override
	public Parameters getParams(){
		return p;
	}

	@Override
	public List<Label> cluster(Dataset dataset){
		
		Random rand=new Random();
		LabelSet labelSet=new LabelSet(p.k);
		List<Label> labels=new ArrayList<Label>(dataset.size());
		for(int i=0;i<dataset.size();i++){
			labels.add(labelSet.newLabel(rand.nextInt(p.k)));
		}
		return labels;
		
	}

}

