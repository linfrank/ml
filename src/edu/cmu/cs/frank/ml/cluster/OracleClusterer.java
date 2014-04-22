package edu.cmu.cs.frank.ml.cluster;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.util.Parameters;

public class OracleClusterer implements Clusterer{

	protected static Logger log=Logger.getLogger(OracleClusterer.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20080512L;

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

		List<Label> labels=new ArrayList<Label>(dataset.size());
		for(int i=0;i<dataset.size();i++){
			labels.add(dataset.getInstance(i).getLabel());
		}
		return labels;
		
	}

}

