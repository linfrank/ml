package edu.cmu.cs.frank.stat;

import java.util.Random;

import edu.cmu.cs.frank.util.Parameters;


public class UniformDiscrete implements Distribution<Integer>,Process<Integer>{
	
	public static class Params extends Parameters{

		static final long serialVersionUID=20091104;

		public int categories=100;
		
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
	public double prob(Integer x){
		if(x>-1&&x<p.categories){
			return 1.0/p.categories;
		}
		else{
			return 0.0;
		}
	}
	
	@Override
	public Integer sample(Random random){
		return random.nextInt(p.categories);
	}
	
	@Override
	public void initProcess(Random random){}
	
	@Override
	public Integer generateNext(Random random){
		return sample(random);
	}

}
