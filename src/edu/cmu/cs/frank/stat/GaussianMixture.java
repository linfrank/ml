package edu.cmu.cs.frank.stat;

import java.util.Random;

import edu.cmu.cs.frank.util.Parameters;


public class GaussianMixture implements Process<Double>{

	public static class Params extends Parameters{

		static final long serialVersionUID=20091104;

		public double[] alpha={5.0,5.0};
		public double clusterVariance=5.0;
		public double dataVariance=0.2;

	}

	private Params p=new Params();

	@Override
	public Params getParams(){
		return p;
	}

	@Override
	public void setParams(Parameters params){
		p=(Params)params;
	}
	
	private Multinomial mult;
	private Gaussian[] mix;
	
	@Override
	public void initProcess(Random random){
		// draw multinomial from dirichlet prior
		Dirichlet dir=new Dirichlet();
		dir.getParams().alpha=p.alpha;
		mult=new Multinomial(dir.sample(random));
		// draw means of the mixture
		Gaussian meansGaussian=new Gaussian(0.0,p.clusterVariance);
		mix=new Gaussian[p.alpha.length];
		for(int i=0;i<mix.length;i++){
			mix[i]=new Gaussian(meansGaussian.sample(random),p.dataVariance);
		}
	}

	@Override
	public Double generateNext(Random random){
		if(mult==null||mix==null){
			initProcess(random);
		}
		// draw data point from cluster
		return mix[mult.sample(random)].sample(random);
	}

}
