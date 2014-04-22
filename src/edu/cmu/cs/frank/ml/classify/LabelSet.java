package edu.cmu.cs.frank.ml.classify;

/**
 * @author Frank Lin
 */

import edu.cmu.cs.frank.util.ArrayUtil;

public class LabelSet extends IndexedSet{

	static final long serialVersionUID=20090127L;
	
	public LabelSet(){}
	
	public LabelSet(String[] labelNames){
		for(int i=0;i<labelNames.length;i++){
			index(labelNames[i]);
		}
	}
	
	public LabelSet(int size){
		for(int i=0;i<size;i++){
			index(String.valueOf(i));
		}
	}
	
	public Label newLabel(int index){
		if(contains(index)){
			return new Label(this,index);
		}
		else{
			return null;
		}
	}
	
	public Label newLabel(double[] weights){
		if(weights.length>size()){
			return null;
		}
		else{
			return new Label(this,ArrayUtil.copy(weights));
		}
	}

	public Label newLabel(String name){
		return new Label(this,index(name));
	}
	
	public Label newLabel(int id,double weight){
		return new Label(this,id,weight);
	}
	
	public Label newLabel(String name,double weight){
		return new Label(this,index(name),weight);
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append("[");
		for(int i=0;i<size();i++){
			if(i>0){
				b.append(" ");
			}
			b.append(i).append("=").append(name(i));
		}
		b.append("]");
		return b.toString();
	}

}
