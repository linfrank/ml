package edu.cmu.cs.frank.util;


public class TimeUtil{
	
	static long lastTic=0;
	
	public static long tic(){
		lastTic=System.currentTimeMillis();
		return lastTic;
	}
	
	public static long toc(long time){
		return System.currentTimeMillis()-time;
	}
	
	public static long toc(){
		return toc(lastTic);
	}

}
