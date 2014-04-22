package edu.cmu.cs.frank.ml.cluster.sim;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.util.Parameters;

public class CosineSim implements Similarity{
	
	static Logger log=Logger.getLogger(CosineSim.class);
	
	public static class Params extends Parameters{

		static final long serialVersionUID=20110411L;

		public double sigma=15;

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
	public double measure(Instance a,Instance b){
		if(a.getFeatureSet()!=b.getFeatureSet()){
			log.error("Trying to measure similarity between instances different feature sets!");
			return 0;
		}
		else{
			Set<Integer> nz=new HashSet<Integer>(a.getFeatures().size()+b.getFeatures().size());
			nz.addAll(a.getFeatures().keySet());
			nz.addAll(b.getFeatures().keySet());
			double dot=0.0;
			double aNorm=0.0;
			double bNorm=0.0;
			for(int i:nz){
				double aVal=a.getFeatureValue(i);
				double bVal=b.getFeatureValue(i);
				dot+=aVal*bVal;
				aNorm+=aVal*aVal;
				bNorm+=bVal*bVal;
			}
			return dot/(Math.sqrt(aNorm)*Math.sqrt(bNorm));
		}
	}
	
}
