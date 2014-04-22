package edu.cmu.cs.frank.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.cmu.cs.frank.util.Parameters;


public class ChineseRestaurantProcess implements Process<Integer>{
	
	public static class Params extends Parameters{

		static final long serialVersionUID=20091104;

		public double alpha=1.0;

	}

	private Params p=new Params();

	@Override
	public Params getParams(){
		return p;
	}

	@Override
	public void setParams(Parameters params){
		p=(Params)params;
	}
	
	private List<Integer> assignments;
	private int numTables=0;
	
	@Override
	public void initProcess(Random random){
		assignments=new ArrayList<Integer>();
	}

	@Override
	public Integer generateNext(Random random){
		if(assignments==null){
			initProcess(random);
		}
		double r=random.nextDouble();
		int assign;
		// sits at a new table
		if(r<p.alpha/(p.alpha+assignments.size())){
			assign=numTables;
			numTables++;
		}
		// sits at an occupied table
		else{
			assign=assignments.get(random.nextInt(assignments.size()));
		}
		assignments.add(assign);
		return assign;
	}

}
