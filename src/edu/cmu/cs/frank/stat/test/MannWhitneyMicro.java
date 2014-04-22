package edu.cmu.cs.frank.stat.test;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.frank.ml.classify.Evaluation;
import edu.cmu.cs.frank.util.ArrayUtil;


public class MannWhitneyMicro extends SigTest{
	
	private boolean[] getRankSample(Evaluation eval){
		List<Boolean> sample=new ArrayList<Boolean>();
		if(SigUtil.isAveraged(eval)){
			List<Evaluation> evals=SigUtil.getAveraging(eval);
			for(Evaluation subEval:evals){
				for(int i=0;i<subEval.getTruth().size();i++){
					if(subEval.getTruth().get(i).equals(subEval.getPrediction().get(i))){
						sample.add(true);
					}
					else{
						sample.add(false);
					}
				}
			}
		}
		else{
			for(int i=0;i<eval.getTruth().size();i++){
				if(eval.getTruth().get(i).equals(eval.getPrediction().get(i))){
					sample.add(true);
				}
				else{
					sample.add(false);
				}
			}
		}
		return ArrayUtil.toBooleanArray(sample);
	}

	@Override
	public void calculate(Evaluation e1,Evaluation e2){

		boolean[] x=getRankSample(e1);
		boolean[] y=getRankSample(e2);
		double[] tails=new double[3];
		
		System.out.println("Sample1 Size: "+x.length);
		System.out.println("Sample2 Size: "+y.length);
		
		MannWhitneyU.mannwhitneyutest2(x,y,tails);
		
		leftTail=tails[MannWhitneyU.LEFT_TAIL];
		rightTail=tails[MannWhitneyU.RIGHT_TAIL];
		oneTail=Math.min(leftTail,rightTail);
		twoTail=tails[MannWhitneyU.BOTH_TAILS];

	}
	
}
