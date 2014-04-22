package edu.cmu.cs.frank.stat.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.cmu.cs.frank.ml.classify.Evaluation;


public abstract class PairedSigTest extends SigTest{

	public abstract void calculate(List<Double> values1,List<Double> values2);

	@Override
	public void calculate(Evaluation e1,Evaluation e2){

		boolean collapsed1=false;
		boolean collapsed2=false;

		if(SigUtil.isAveraged(e1)){
			e1=SigUtil.randomCollapse(SigUtil.getAveraging(e1));
			collapsed1=true;
		}

		if(SigUtil.isAveraged(e2)){
			e2=SigUtil.randomCollapse(SigUtil.getAveraging(e2));
			collapsed2=true;
		}

		List<Double> values1=new ArrayList<Double>(e1.getTruth().size());
		List<Double> values2=new ArrayList<Double>(e2.getTruth().size());

		Set<String> ids=e1.getIds();
		ids.retainAll(e2.getIds());
		for(String id:ids){
			double v1=e1.getTruth(id).equals(e1.getPrediction(id))?1:0;
			double v2=e2.getTruth(id).equals(e2.getPrediction(id))?1:0;
			values1.add(v1);
			values2.add(v2);
		}

		StringBuilder b=new StringBuilder();
		b.append("Sample1 Size: ").append(e1.getTruth().size());
		if(collapsed1){
			b.append(" ").append("(Collapsed)");
		}
		b.append("\n");
		b.append("Sample2 Size: ").append(e2.getPrediction().size());
		if(collapsed2){
			b.append(" ").append("(Collapsed)");
		}
		b.append("\n");
		b.append("Matched Pairs: ").append(values1.size()).append("\n");
		System.out.println(b.toString());
		
		calculate(values1,values2);

	}

}
