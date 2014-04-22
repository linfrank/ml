package edu.cmu.cs.frank.ml.graph;

import edu.cmu.cs.frank.ml.classify.Label;

public class LabelSticky extends StickyId{
	
	public Label label;
	
	public LabelSticky(Object id,Label label){
		super(id);
		this.label=label;
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(super.toString());
		b.append("[Label: ").append(label).append("]").toString();
		return b.toString();
	}

}
