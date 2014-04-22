package edu.cmu.cs.frank.stat.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.cmu.cs.frank.ml.classify.Evaluation;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.LabelSet;
import edu.cmu.cs.frank.util.ArrayUtil;

public class SigUtil{

	public static boolean isAveraged(Evaluation eval){
		try{
			Field field=Evaluation.class.getDeclaredField("averaged");
			field.setAccessible(true);
			return field.getBoolean(eval);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Evaluation> getAveraging(Evaluation eval){
		try{
			Field field=Evaluation.class.getDeclaredField("averaging");
			field.setAccessible(true);
			return (List<Evaluation>)field.get(eval);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static Evaluation foldCollapse(List<Evaluation> evals){

		Set<String> ids=new HashSet<String>();
		for(Evaluation eval:evals){
			ids.addAll(eval.getIds());
		}
		List<String> idList=new ArrayList<String>(ids);
		Collections.shuffle(idList);

		int foldSize=(int)Math.ceil((double)ids.size()/evals.size());

		LabelSet labelSet=evals.get(0).getLabelSet();
		Map<String,Integer> idMap=new HashMap<String,Integer>();
		List<Label> truth=new ArrayList<Label>(ids.size());
		List<Label> prediction=new ArrayList<Label>(ids.size());

		int index=0;
		int fold=0;
		for(int i=0;i<idList.size();i++){
			
			String id=idList.get(i);

			if(evals.get(fold).getIdMap().containsKey(id)){

				idMap.put(id,index);
				truth.add(evals.get(fold).getTruth(id));
				prediction.add(evals.get(fold).getPrediction(id));

				index++;
			}
			
			if((i+1)%foldSize==0){
				fold++;
			}
			
		}

		return new Evaluation(labelSet,truth,prediction,idMap);

	}

	public static Evaluation randomCollapse(List<Evaluation> evals){

		Set<String> ids=new HashSet<String>();
		for(Evaluation eval:evals){
			ids.addAll(eval.getIds());
		}

		LabelSet labelSet=evals.get(0).getLabelSet();
		Map<String,Integer> idMap=new HashMap<String,Integer>();
		Label[] truth=new Label[ids.size()];
		Label[] prediction=new Label[ids.size()];
		Random random=new Random();

		int index=0;
		for(String id:ids){
			idMap.put(id,index);

			for(int i=0;i<evals.size();i++){
				if(evals.get(i).getIdMap().containsKey(id)){
					truth[index]=evals.get(i).getTruth(id);
					break;
				}
			}

			List<Label> possible=new ArrayList<Label>(evals.size());
			for(int i=0;i<evals.size();i++){
				if(evals.get(i).getIdMap().containsKey(id)){
					possible.add(evals.get(i).getPrediction(id));
				}
			}
			prediction[index]=possible.get(random.nextInt(possible.size()));

			index++;
		}

		return new Evaluation(labelSet,ArrayUtil.toList(truth),ArrayUtil.toList(prediction),idMap);

	}

}
