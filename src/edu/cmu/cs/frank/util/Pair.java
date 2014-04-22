package edu.cmu.cs.frank.util;

import java.util.Comparator;


public class Pair<A,B>{
	
	A a;
	B b;
	
	public Pair(A a,B b){
		this.a=a;
		this.b=b;
	}
	
	public Pair(){
		this(null,null);
	}
	
	public void setA(A a){
		this.a=a;
	}
	
	public A getA(){
		return a;
	}
	
	public void setB(B b){
		this.b=b;
	}
	
	public B getB(){
		return b;
	}
	
	public Comparator<Pair<A,B>> getAComparator(){
		return new Comparator<Pair<A,B>>(){
			@Override
			@SuppressWarnings("unchecked")
			public int compare(Pair<A,B> p1,Pair<A,B> p2){
				return ((Comparable<A>)p1.getA()).compareTo(p2.getA());
			}
		};
	}
	
	public Comparator<Pair<A,B>> getBComparator(){
		return new Comparator<Pair<A,B>>(){
			@Override
			@SuppressWarnings("unchecked")
			public int compare(Pair<A,B> p1,Pair<A,B> p2){
				return ((Comparable<B>)p1.getB()).compareTo(p2.getB());
			}
		};
	}
	
	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append("{").append(a).append(",").append(b).append("}");
		return b.toString();
	}

}
