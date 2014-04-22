package edu.cmu.cs.frank.util;

import java.io.PrintStream;

public class UnicodeTest{
	
	/**
	 * The main method should produce the following Chinese sentence:
	 * 天蒼蒼，野茫茫，風吹草低見牛羊。
	 * 
	 * @param args ignored
	 */
	
	public static void main(String[] args)throws Exception{
		PrintStream ps=new PrintStream(System.out,true,"utf8");
		ps.println("天蒼蒼，野茫茫，風吹草低見牛羊。");
	}

}
