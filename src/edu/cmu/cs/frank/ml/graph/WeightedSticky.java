package edu.cmu.cs.frank.ml.graph;

public class WeightedSticky extends StickyId{
	
	public double weight;
	
	public WeightedSticky(Object id,double weight){
		super(id);
		this.weight=weight;
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(super.toString());
		b.append(" [Weight: ").append(weight).append("]");
		return b.toString();
	}
	
}
