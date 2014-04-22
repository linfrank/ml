package edu.cmu.cs.frank.ml.cluster.sim;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.util.Parameters;

public class EuclideanSim implements Similarity{

	static Logger log=Logger.getLogger(EuclideanSim.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20110411L;

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
		return 1.0/(1.0+distance(a,b));
	}

	public static double distance(Instance a,Instance b){
		if(a.getFeatureSet()!=b.getFeatureSet()){
			log.error("Trying to measure similarity between different feature sets!");
			return 0;
		}
		else{
			Set<Integer> nz=new HashSet<Integer>(a.getFeatures().size()+b.getFeatures().size());
			nz.addAll(a.getFeatures().keySet());
			nz.addAll(b.getFeatures().keySet());
			double sum=0.0;
			for(int i:nz){
				double diff=a.getFeatureValue(i)-b.getFeatureValue(i);
				sum+=diff*diff;
			}
			return Math.sqrt(sum);
		}
	}

}
