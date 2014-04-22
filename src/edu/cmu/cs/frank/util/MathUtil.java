package edu.cmu.cs.frank.util;


public class MathUtil{
	
	public static double log(double x,double base){
		return Math.log(x)/Math.log(base);
	}
	
	public static double log2(double x){
		return log(x,2.0);
	}

}
