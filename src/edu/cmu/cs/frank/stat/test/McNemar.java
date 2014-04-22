package edu.cmu.cs.frank.stat.test;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * NcNemar's test
 * 
 * Code translated from:
 * http://faculty.vassar.edu/lowry/propcorr.html
 * 
 * @author Frank Lin
 */

public class McNemar extends PairedSigTest{
	
	static Logger log=Logger.getLogger(McNemar.class);

	private int x2Threshold;

	public McNemar(int x2Threshold){
		this.x2Threshold=x2Threshold;
	}

	public McNemar(){
		this(1000);
	}

	@Override
	public void calculate(List<Double> v1,List<Double> v2){

		int b=0;
		int c=0;

		for(int i=0;i<v1.size();i++){
			int value1=(int)v1.get(i).doubleValue();
			int value2=(int)v2.get(i).doubleValue();
			if(value1!=value2){
				if(value1>value2){
					b++;
				}
				else{
					c++;
				}
			}
		}
		
		log.info("b="+b+" c="+c);
		
		calculate(b,c);

	}
	
	private void calculate(int b,int c){
		
		int nb=b+c;
		int kb=Math.max(b,c);
		
		if(nb>x2Threshold){
			oneTail=x2(nb,kb);
		}
		else{
			oneTail=exact(nb,kb);
		}
		
		twoTail=oneTail*2;
		
	}
	
	// returns one-tail
	
	private double x2(int nb,int kb){
		double z=kb-((double)nb/2)-0.5;
		z=z/Math.sqrt(nb*0.25);
		z=Math.abs(z);
		double p2=(((((.000005383*z+.0000488906)*z+.0000380036)*z+.0032776263)*z+.0211410061)*z+.049867347)*z+1;
		p2=Math.pow(p2,-16);
		return p2/2;
	}
	
	// returns one-tail

	private double exact(int nb,int kb){

		double factN=0;
		for(int i=2;i<=nb;i++){
			factN+=Math.log(i);
		}
		
		double[] tabZ=new double[nb+1];
		for(int i=0;i<nb+1;i++){
			
			double factR=0;
			for(int j=2;j<=i;j++){
				factR+=Math.log(j);
			}
			
			double nr=(double)nb-i;
			
			double factNR=0;
			for(int j=2;j<=nr;j++){
				factNR+=Math.log(j);
			}
			
			double a=factN-(factR+factNR);
			a=Math.exp(a);
			double b=Math.pow(0.5,i)*Math.pow(0.5,nb-i);
			
			tabZ[i]=a*b;
			
		}
		
		double sum=0;
		for(int i=kb;i<nb+1;i++){
			sum+=tabZ[i];
		}
		
		return sum;

	}
	
	public static void main(String[] args){
		
		if(args.length!=2){
			System.out.println("Usage:");
			System.out.println("java "+McNemar.class.getSimpleName()+" B C");
			return;
		}
		
		int b=Integer.parseInt(args[0]);
		int c=Integer.parseInt(args[1]);
		
		McNemar m=new McNemar();
		m.calculate(b,c);
		
		System.out.println("Two-Tail: "+m.twoTail);
		System.out.println("One-Tail: "+m.oneTail);
		
	}

}
