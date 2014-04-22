package edu.cmu.cs.frank.stat;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import edu.cmu.cs.frank.util.Parameters;


public class Gamma implements Distribution<Double>,Process<Double>{

	public static class Params extends Parameters{

		static final long serialVersionUID=20091104;

		public double k=1.0;
		public double theta=1.0;

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
	public double prob(Double x){
		// not implemented yet
		return -1.0;
	}

	// the acceptance-rejection method vectors V, xi, and eta 
	private Map<Integer,Double> arV=new HashMap<Integer,Double>();
	private Map<Integer,Double> arXi=new HashMap<Integer,Double>();
	private Map<Integer,Double> arEta=new HashMap<Integer,Double>();

	// exactly the acceptance-rejection method in wikipedia
	@Override
	public Double sample(Random random){

		int kInt=(int)p.k;
		double kFrac=p.k-kInt;
		double xi;
		
		if(kFrac!=0.0){
			// step 1
			int m=1;
			while(true){
				// step 2
				arV.put(3*m-2,random.nextDouble());
				arV.put(3*m-1,random.nextDouble());
				arV.put(3*m,random.nextDouble());
				// step 3
				double v0=Math.E/(Math.E+kFrac);
				if(arV.get(3*m-2)<=v0){
					// step 4
					arXi.put(m,Math.pow(arV.get(3*m-1),1.0/kFrac));
					arEta.put(m,arV.get(3*m)*Math.pow(arXi.get(m),kFrac-1.0));
				}
				else{
					// step 5
					arXi.put(m,1.0-Math.log(arV.get(3*m-1)));
					arEta.put(m,arV.get(3*m)*Math.exp(-arXi.get(m)));
				}
				// step 6
				if(arEta.get(m)>Math.pow(arXi.get(m),kFrac-1.0)*Math.exp(-arXi.get(m))){
					m++;
				}
				else{
					break;
				}
			}
			// step 7
			xi=arXi.get(m);
		}
		else{
			xi=0.0;
		}
		
		double uSum=0.0;
		for(int i=0;i<kInt;i++){
			uSum+=Math.log(random.nextDouble());
		}
		return p.theta*(xi-uSum);
		
	}
	
	@Override
	public void initProcess(Random random){}
	
	@Override
	public Double generateNext(Random random){
		return sample(random);
	}
	
	public static void main(String[] args){
		Random random=new Random();
		Gamma gamma=new Gamma();
		gamma.p.k=3.2;
		gamma.p.theta=2.0;
		int trials=5000;
		int[] bins=new int[50];
		for(int i=0;i<trials;i++){
			double sample=gamma.sample(random);
			bins[(int)sample]++;
		}
		for(int i=0;i<bins.length;i++){
			System.out.print(i+"\t");
			bins[i]=bins[i]*300/trials;
			for(int j=0;j<bins[i];j++){
				System.out.print("*");
			}
			System.out.println();
		}
	}

}
