package edu.cmu.cs.frank.ml.classify;

/**
 * @author Frank Lin
 */

public class BinaryLabel extends Label{
	
	static final long serialVersionUID=20071112L;
	
	public static final LabelSet BINARY=new LabelSet(new String[]{"POS","NEG"});
	
	public static final BinaryLabel POS=new BinaryLabel(BINARY,0);
	public static final BinaryLabel NEG=new BinaryLabel(BINARY,1);
	
	private BinaryLabel(LabelSet set,int id){
		super(set,id);
	}
	
	public int getPolarValue(){
		if(this.equals(NEG)){
			return -1;
		}
		else{
			return 1;
		}
	}
	
	public static BinaryLabel not(BinaryLabel label){
		if(label.equals(NEG)){
			return POS;
		}
		else{
			return NEG;
		}
	}
	
}