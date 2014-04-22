package edu.cmu.cs.frank.x;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.rank.quality.AP;
import edu.cmu.cs.frank.ml.rank.quality.NDCG;
import edu.cmu.cs.frank.ml.rank.quality.PrecisionAtRecall;
import edu.cmu.cs.frank.ml.rank.quality.RankQualityMeasure;
import edu.cmu.cs.frank.util.IOUtil;
import edu.cmu.cs.frank.util.ScoreTable;
import edu.cmu.cs.frank.util.TimeUtil;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;


public class DBLPAuthorAmbRank{

	static Logger log=Logger.getLogger(DBLPAuthorAmbRank.class);

	static final Pattern DIGITS=Pattern.compile("\\d+");

	public static void main(String[] args)throws Exception{

		if(args.length!=5){
			System.out.println("Usage: <flat file> <num refs> <out prefix> <curator> <rm dup auth?>");
			return;
		}

		File input=new File(args[0]);
		int numRefs=Integer.parseInt(args[1]);
		String outputPrefix=args[2];
		String curatorName=args[3];
		boolean rmDupAuth=Boolean.parseBoolean(args[4]);
		
		outputPrefix=outputPrefix+input.getName().substring(0,input.getName().lastIndexOf('.'))+"-"+curatorName;
		
		Curator curator=getCurator(curatorName);

		// the data, nodes are author names or papers
		DirectedGraph<String,Integer> graph=new DirectedSparseGraph<String,Integer>();

		// the (pseudo-)ground truth, maps an edge (author instance) to an author id
		Map<Integer,String> truth=new HashMap<Integer,String>(numRefs);

		// keeping track of duplicate names on the same paper
		Set<Integer> dupNames=new HashSet<Integer>();

		log.info("Reading data from "+input);
		TimeUtil.tic();

		BufferedReader reader=IOUtil.getBufferedReader(input,"utf8");

		int i=0;
		for(String line;(line=reader.readLine())!=null;i++){

			String[] tokens=line.split("\\t");

			String id=tokens[0].trim();
			String name="N:"+tokens[1].trim();
			String paper="P:"+tokens[2].trim();

			if(!graph.addEdge(i,name,paper)){
				// add to set of duplicate name-paper instances
				dupNames.add(graph.findEdge(name,paper));
			}

			truth.put(i,id);

		}
		reader.close();

		log.info("Found "+dupNames.size()+" instances of duplicate name-paper authorship");
		if(rmDupAuth){
			log.info("Removing ALL duplicated name-paper authorships from graph");
			for(Integer instance:dupNames){
				graph.removeEdge(instance);
			}
		}

		log.info("Finished reading in "+(TimeUtil.toc()/1000)+" s");
		log.info("Graph contains "+graph.getVertexCount()+" vertices and "+graph.getEdgeCount()+" edges");

		log.info("Ranking ambiguity: "+curatorName);
		TimeUtil.tic();

		ScoreTable<String> scores=curator.getAmbiguityScore(graph);

		log.info("Finished in "+(TimeUtil.toc()/1000)+" s");

		log.info("Evaluating and writing analysis and results");

		TimeUtil.tic();

		Set<String> ambiguous=new HashSet<String>();
		for(String name:graph.getVertices()){
			// only evaluate author names
			if(name.startsWith("N:")){
				List<Integer> instances=new ArrayList<Integer>(graph.getOutEdges(name));
				if(instances.size()>1&&!uniform(instances,truth)){
					ambiguous.add(name);
				}
			}
		}

		List<String> ranking=scores.sortedKeysDescend();

		PrintWriter rankWriter=IOUtil.getPrintWriter(outputPrefix+"-rank.txt","utf8",true);
		for(String name:ranking){
			rankWriter.print(name);
			rankWriter.print("\t");
			rankWriter.println(scores.get(name));
		}
		rankWriter.close();

		List<RankQualityMeasure<String>> rqms=new LinkedList<RankQualityMeasure<String>>();
		rqms.add(new NDCG<String>());
		rqms.add(new AP<String>());
		rqms.add(new PrecisionAtRecall<String>(0.005));
		rqms.add(new PrecisionAtRecall<String>(0.01));
		rqms.add(new PrecisionAtRecall<String>(0.05));
		rqms.add(new PrecisionAtRecall<String>(0.1));
		rqms.add(new PrecisionAtRecall<String>(0.5));
		rqms.add(new PrecisionAtRecall<String>(1.0));
				
		PrintWriter evalWriter=IOUtil.getPrintWriter(outputPrefix+"-rank_eval.txt","utf8",false);
		for(RankQualityMeasure<String> rqm:rqms){
			evalWriter.print(rqm);
			evalWriter.print("\t");
			evalWriter.println(rqm.measure(ranking,ambiguous));
		}
		evalWriter.close();
		
		PrintWriter expWriter=IOUtil.getPrintWriter(outputPrefix+"-rank_explain.txt","utf8",false);
		int sampleSize=2000;
		int interval=ranking.size()/sampleSize;
		i=0;
		while(i<ranking.size()){
			expWriter.print(getExplanation(ranking.get(i),i+1,graph,scores));
			i+=interval;
		}
		expWriter.close();

		log.info("Finished evaluation in "+(TimeUtil.toc()/1000)+" s");

	}
	
	private static String getExplanation(String name,int rank,DirectedGraph<String,Integer> graph,ScoreTable<String> scores){
		StringBuilder b=new StringBuilder();
		b.append("= Explanation for \"").append(name).append("\" Rank ").append(rank).append("(").append(scores.get(name)).append(") =").append("\n");
		for(String paper:graph.getSuccessors(name)){
			b.append(" ").append(paper).append(" ").append(graph.getPredecessors(paper)).append("\n");
		}
		return b.toString();
	}

	private static boolean uniform(Collection<Integer> instances,Map<Integer,String> map){

		if(instances.size()<=1){
			return true;
		}

		boolean first=true;
		String ref=null;
		for(int instance:instances){
			String id=map.get(instance);
			if(first){
				ref=id;
				first=false;
			}
			else{
				if(id==null){
					if(ref!=null){
						return false;
					}
				}
				else{
					if(!id.equals(ref)){
						return false;
					}
				}
			}
		}
		return true;
	}

	private static Curator getCurator(String name){

		Curator curator;

		if(name.equalsIgnoreCase("rand")){
			curator=new RandomDisambiguator();
		}
		else if(name.equalsIgnoreCase("dumb")){
			curator=new DumbDisambiguator();
		}
		else if(name.equalsIgnoreCase("base")){
			curator=new ComponentDisambiguator(false);
		}
		else if(name.equalsIgnoreCase("base2")){
			curator=new ComponentDisambiguator(true);
		}
		else if(name.equalsIgnoreCase("modularity")){
			curator=new ModularityDisambiguator();
		}
		else if(name.equalsIgnoreCase("oer")){
			curator=new OutEdgeRatioDisambiguator(false);
		}
		else if(name.equalsIgnoreCase("noer")){
			curator=new OutEdgeRatioDisambiguator(true);
		}
		else if(name.equalsIgnoreCase("cc")){
			curator=new ClusteringCoefficientDisambiguator();
		}
		else if(name.equalsIgnoreCase("pic")){
			curator=new PICDisambiguator(13);
		}
		else{
			log.error("Does not recognize curator "+name);
			curator=null;
		}

		return curator;

	}

}
