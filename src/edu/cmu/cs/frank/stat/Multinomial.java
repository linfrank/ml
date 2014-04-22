package edu.cmu.cs.frank.stat;

import java.util.Random;

import cern.colt.Arrays;
import edu.cmu.cs.frank.util.Parameters;


public class Multinomial implements Distribution<Integer>{
	
	public static class Params extends Parameters{

		static final long serialVersionUID=20091104;

		public double[] probs={0.5,0.5};

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
	
	public Multinomial(double[] probs){
		p=new Params();
		p.probs=probs;
	}
	
	public Multinomial(){
		p=new Params();
	}
	
	@Override
	public double prob(Integer x){
		if(x>-1&&x<p.probs.length){
			return p.probs[x];
		}
		else{
			return 0.0;
		}
	}
	
	@Override
	public Integer sample(Random random){
		double probSum=0.0;
		double r=random.nextDouble();
		for(int i=0;i<p.probs.length-1;i++){
			probSum+=p.probs[i];
			if(r<probSum){
				return i;
			}
		}
		return p.probs.length-1;
	}
	
	public static void main(String[] args){
		Random random=new Random();
		double[] probs={0.6,0.3,0.1};
		Multinomial mult=new Multinomial(probs);
		int trials=50;
		int[] counts=new int[probs.length];
		for(int i=0;i<trials;i++){
			int result=mult.sample(random);
			System.out.println("Trial "+i+": "+result);
			counts[result]++;
		}
		System.out.println("Counts: "+Arrays.toString(counts));
	}

}
