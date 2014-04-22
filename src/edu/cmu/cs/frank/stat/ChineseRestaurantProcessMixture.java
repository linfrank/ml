package edu.cmu.cs.frank.stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.cmu.cs.frank.util.Parameters;


public class ChineseRestaurantProcessMixture implements Process<Double>{
	
	public static class Params extends Parameters{

		static final long serialVersionUID=20091104;

		public double alpha=1.0;
		public double clusterVariance=1.0;
		public double dataVariance=0.2;

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
	
	private ChineseRestaurantProcess crp;
	private Gaussian tableMeans;
	private List<Gaussian> tables;
	
	@Override
	public void initProcess(Random random){
		crp=new ChineseRestaurantProcess();
		crp.getParams().alpha=p.alpha;
		tableMeans=new Gaussian(0.0,p.clusterVariance);
		tables=new ArrayList<Gaussian>();
	}

	@Override
	public Double generateNext(Random random){
		if(crp==null||tableMeans==null||tables==null){
			initProcess(random);
		}
		int assign=crp.generateNext(random);
		// sits at a new table
		if(assign>=tables.size()){
			tables.add(new Gaussian(tableMeans.sample(random),p.dataVariance));
		}
		return tables.get(assign).sample(random);
	}

}
