package edu.cmu.cs.frank.stat;

import java.util.Random;

import edu.cmu.cs.frank.util.Parameters;


public class Gaussian implements Distribution<Double>,Process<Double>{
	
	public static class Params extends Parameters{

		static final long serialVersionUID=20091104;

		public double mean=0.0;
		public double variance=1.0;

	}

	private Params p;

	@Override
	public Params getParams(){
		return p;
	}

	@Override
	public void setParams(Parameters params){
		p=(Params)params;
	}
	
	public Gaussian(double mean,double variance){
		p=new Params();
		p.mean=mean;
		p.variance=variance;
	}
	
	public Gaussian(){
		p=new Params();
	}
	
	@Override
	public double prob(Double x){
		double err=x-p.mean;
		return Math.exp(-(err*err)/2*p.variance)/Math.sqrt(2*Math.PI*p.variance);
	}
	
	@Override
	public Double sample(Random random){
		return random.nextGaussian()*p.variance+p.mean;
	}
	
	@Override
	public void initProcess(Random random){}
	
	@Override
	public Double generateNext(Random random){
		return sample(random);
	}

}
