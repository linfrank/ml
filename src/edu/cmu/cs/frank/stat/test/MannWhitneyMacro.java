package edu.cmu.cs.frank.stat.test;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.frank.ml.classify.Evaluation;
import edu.cmu.cs.frank.util.ArrayUtil;


public class MannWhitneyMacro extends SigTest{

	private double[] getRankSample(Evaluation eval){
		List<Double> sample=new ArrayList<Double>();
		if(SigUtil.isAveraged(eval)){
			List<Evaluation> evals=SigUtil.getAveraging(eval);
			for(Evaluation subEval:evals){
				sample.add(subEval.accuracy);
			}
		}
		else{
			sample.add(eval.accuracy);
		}
		return ArrayUtil.toDoubleArray(sample);
	}

	@Override
	public void calculate(Evaluation e1,Evaluation e2){

		double[] x=getRankSample(e1);
		double[] y=getRankSample(e2);
		double[] tails=new double[3];

		System.out.println("Sample1 Size: "+x.length);
		System.out.println("Sample2 Size: "+y.length);

		MannWhitneyU.mannwhitneyutest(x,x.length,y,y.length,tails);

		leftTail=tails[MannWhitneyU.LEFT_TAIL];
		rightTail=tails[MannWhitneyU.RIGHT_TAIL];
		oneTail=Math.min(leftTail,rightTail);
		twoTail=tails[MannWhitneyU.BOTH_TAILS];

	}

}
