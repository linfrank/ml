package edu.cmu.cs.frank.stat;

import java.util.Random;

import edu.cmu.cs.frank.util.Parameters;


public class UniformContinuous implements Distribution<Double>,Process<Double>{
	
	public static class Params extends Parameters{

		static final long serialVersionUID=20091104;

		public double minInclude=0.0;
		public double maxExclude=1.0;
		
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
	
	@Override
	public double prob(Double T){
		return 1.0/(p.maxExclude-p.minInclude);
	}
	
	@Override
	public Double sample(Random random){
		return random.nextDouble()*(p.maxExclude-p.minInclude)+p.minInclude;
	}
	
	@Override
	public void initProcess(Random random){}
	
	@Override
	public Double generateNext(Random random){
		return sample(random);
	}
	
}
