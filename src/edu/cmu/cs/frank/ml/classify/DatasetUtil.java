package edu.cmu.cs.frank.ml.classify;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.IOUtil;

/**
 * Loads data
 * 
 * @author Frank Lin
 */

public class DatasetUtil{

	static Logger log=Logger.getLogger(DatasetUtil.class);

	public static LinkedHashMap<String,Instance> loadInstances(File file,FeatureSet featureSet){
		LinkedHashMap<String,Instance> instances=new LinkedHashMap<String,Instance>();
		try{
			BufferedReader reader=IOUtil.getBufferedReader(file,"utf8");
			for(String line;(line=reader.readLine())!=null;){
				String[] tokens=line.split("\\s+");
				Instance instance=new Instance(featureSet);
				instance.setId(tokens[0]);
				for(int i=1;i<tokens.length;i++){
					String[] feature=tokens[i].split("\\=");
					if(feature[0].length()<1||feature[1].length()<1){
						System.err.println("Bad feature token: "+tokens[i]);
					}
					instance.setFeature(feature[0],Double.parseDouble(feature[1]));
				}
				instances.put(tokens[0],instance);
			}
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return instances;
	}

	public static LinkedHashMap<String,Label> loadLabels(File file,LabelSet labelSet){
		LinkedHashMap<String,Label> labels=new LinkedHashMap<String,Label>();
		try{
			BufferedReader reader=IOUtil.getBufferedReader(file,"utf8");
			for(String line;(line=reader.readLine())!=null;){
				String[] tokens=line.split("\\s+");
				labels.put(tokens[0],labelSet.newLabel(tokens[1]));
			}
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return labels;
	}

	public static Dataset load(File featureFile,File labelFile){
		Dataset dataset=new Dataset();
		LinkedHashMap<String,Instance> instanceMap=loadInstances(featureFile,dataset.getFeatureSet());
		LinkedHashMap<String,Label> labelMap=null;
		Set<String> ids=new HashSet<String>();
		ids.addAll(instanceMap.keySet());
		if(labelFile!=null){
			labelMap=loadLabels(labelFile,dataset.getLabelSet());
			ids.addAll(labelMap.keySet());
		}
		for(String id:ids){
			Instance instance=instanceMap.get(id);
			if(instance==null){
				instance=new Instance(id,dataset.getFeatureSet());
			}
			if(labelMap!=null){
				instance.setLabel(labelMap.get(id));
			}
			dataset.add(instance);
		}
		return dataset;
	}

	public static Dataset load(String featureFileName,String labelFileName){
		return load(new File(featureFileName),new File(labelFileName));
	}
	
	public static Dataset load(File featureFile){
		return load(featureFile,null);
	}
	
	public static Dataset load(String featureFileName){
		return load(new File(featureFileName));
	}
	
	public static Dataset loadFromName(String datasetName,boolean relational){

		String labelFile="./data/"+datasetName+"/"+datasetName+".label";
		String featureFile;
		if(relational){
			featureFile="./data/"+datasetName+"/"+datasetName+".relation";
		}
		else{
			featureFile="./data/"+datasetName+"/"+datasetName+".feature";
		}

		return DatasetUtil.load(featureFile,labelFile);

	}

	public static void saveFeatures(Dataset dataset,File featureFile){
		PrintWriter writer=IOUtil.getPrintWriter(featureFile,"utf8",true);
		for(Instance instance:dataset){
			writer.print(instance.getId());
			for(int featureId:instance.getFeatures().keySet()){
				writer.print(" ");
				writer.print(instance.getFeatureName(featureId));
				writer.print("=");
				writer.print(instance.getFeatureValue(featureId));
			}
			writer.println();
		}
		writer.close();
	}

	public static void saveLabels(Dataset dataset,File labelFile){
		PrintWriter writer=IOUtil.getPrintWriter(labelFile,"utf8",true);
		for(Instance instance:dataset){
			writer.print(instance.getId());
			writer.print("\t");
			writer.print(instance.getLabel().getBestName());
			writer.println();
		}
		writer.close();
	}
	
	public static void saveLabels(Dataset dataset,List<Label> labels,File labelFile){
		PrintWriter writer=IOUtil.getPrintWriter(labelFile,"utf8",true);
		for(int i=0;i<dataset.size();i++){
			writer.print(dataset.getInstance(i).getId());
			writer.print("\t");
			writer.print(labels.get(i).getBestName());
			writer.println();
		}
		writer.close();
	}

	public static void save(Dataset dataset,File featureFile,File labelFile){
		saveFeatures(dataset,featureFile);
		saveLabels(dataset,labelFile);
	}

	public static void save(Dataset dataset,String datasetPath,String datasetName){
		if(dataset.getLabelSet().size()>0){
			File labelFile=new File(datasetPath+"/"+datasetName+".label");
			saveLabels(dataset,labelFile);
		}
		if(dataset.getFeatureSet().size()>0){
			File featureFile=new File(datasetPath+"/"+datasetName+".feature");
			saveFeatures(dataset,featureFile);
		}
	}


}
