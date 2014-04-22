package edu.cmu.cs.frank.util;

import java.text.DecimalFormat;


public class FormatUtil{
	
	public static final DecimalFormat D1=new DecimalFormat("0.0");
	
	public static String d1(double d){
		return D1.format(d);
	}
	
	public static final DecimalFormat D2=new DecimalFormat("0.00");
	
	public static String d2(double d){
		return D2.format(d);
	}
	
	public static final DecimalFormat D3=new DecimalFormat("0.000");
	
	public static String d3(double d){
		return D3.format(d);
	}
	
	public static final DecimalFormat D4=new DecimalFormat("0.0000");
	
	public static String d4(double d){
		return D4.format(d);
	}
	
	public static final DecimalFormat D5=new DecimalFormat("0.00000");
	
	public static String d5(double d){
		return D5.format(d);
	}
	
	public static final DecimalFormat D6=new DecimalFormat("0.000000");
	
	public static String d6(double d){
		return D6.format(d);
	}
	
	public static final DecimalFormat D7=new DecimalFormat("0.0000000");
	
	public static String d7(double d){
		return D7.format(d);
	}
	
	public static final DecimalFormat D8=new DecimalFormat("0.00000000");
	
	public static String d8(double d){
		return D8.format(d);
	}
	
	public static String s3(double d){
		if(d<10.0){
			return D2.format(d);
		}
		else if(d<100.0){
			return D1.format(d);
		}
		else{
			return String.valueOf((int)d);
		}
	}

}
