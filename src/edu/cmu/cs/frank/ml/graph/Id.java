package edu.cmu.cs.frank.ml.graph;

public class Id{
	
	public final Object id;
	
	public Id(Object id){
		this.id=id;
	}

	@Override
	public boolean equals(Object o){
		return id.equals(o);
	}

	@Override
	public int hashCode(){
		return id.hashCode();
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		return b.append("[ID: ").append(id).append("]").toString();
	}

}
