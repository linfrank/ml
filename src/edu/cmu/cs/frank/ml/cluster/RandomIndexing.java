package edu.cmu.cs.frank.ml.cluster;

import java.util.List;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.util.Parameters;

/**
 * @author Frank Lin
 * 
 */

public class RandomIndexing implements Clusterer{

	protected static Logger log=Logger.getLogger(RandomIndexing.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20111003;

		public int initialK=2;

	}

	private Params p;

	@Override
	public void setParams(Parameters params){
		this.p=(Params)params;
	}

	@Override
	public Params getParams(){
		return p;
	}
	
	@Override
	public List<Label> cluster(Dataset dataset){

		return null;

	}

}
