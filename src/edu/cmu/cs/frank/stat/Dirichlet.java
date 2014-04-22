package edu.cmu.cs.frank.stat;

import java.util.Random;

import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.Parameters;


public class Dirichlet implements Distribution<double[]>{
	
	public static class Params extends Parameters{

		static final long serialVersionUID=20091104;

		public double[] alpha={2.0,2.0};

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
	public double prob(double[] x){
		// not implemented yet
		return -1.0;
	}
	
	private Gamma gamma=new Gamma();
	
	@Override
	public double[] sample(Random random){
		
		double[] sample=new double[p.alpha.length];
		
		Gamma.Params gParams=new Gamma.Params();
		for(int i=0;i<sample.length;i++){
			gParams.k=p.alpha[i];
			gParams.theta=1.0;
			gamma.setParams(gParams);
			sample[i]=gamma.sample(random);
		}
		
		ArrayUtil.normalize(sample);
		
		return sample;
	}
	
	public static void main(String[] args){
		Random random=new Random();
		Dirichlet dir=new Dirichlet();
		dir.p.alpha=new double[]{5.0,2.0};
		int trials=5000;
		int[] bins=new int[50];
		for(int i=0;i<trials;i++){
			double[] sample=dir.sample(random);
			bins[(int)(sample[0]*50)]++;
		}
		for(int i=0;i<bins.length;i++){
			System.out.print(i+"\t");
			bins[i]=bins[i]*500/trials;
			for(int j=0;j<bins[i];j++){
				System.out.print("*");
			}
			System.out.println();
		}
		
	}

}
