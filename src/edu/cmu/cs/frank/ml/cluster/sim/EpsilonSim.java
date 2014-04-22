package edu.cmu.cs.frank.ml.cluster.sim;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.util.Parameters;

public class EpsilonSim implements Similarity{
	
	static Logger log=Logger.getLogger(EpsilonSim.class);
	
	public static class Params extends Parameters{

		static final long serialVersionUID=20110411L;

		public double epsilon=10;

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
		if(EuclideanSim.distance(a,b)>p.epsilon){
			return 0;
		}
		else{
			return 1;
		}
	}
	
}
