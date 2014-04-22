package edu.cmu.cs.frank.x;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.LabelSet;
import edu.cmu.cs.frank.ml.cluster.quality.ExternalCQM;
import edu.cmu.cs.frank.ml.cluster.quality.NMI;
import edu.cmu.cs.frank.ml.cluster.quality.Purity;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.FormatUtil;
import edu.cmu.cs.frank.util.IOUtil;
import edu.cmu.cs.frank.util.MapUtil;
import edu.cmu.cs.frank.util.TimeUtil;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;


public class DBLPAuthorAmb{

	static Logger log=Logger.getLogger(DBLPAuthorAmb.class);

	static final Pattern DIGITS=Pattern.compile("\\d+");

	public static void main(String[] args)throws Exception{

		if(args.length!=5){
			System.out.println("Usage: <flat file> <num refs> <out prefix> <curator> <rm dup auth?>");
			return;
		}

		String input=args[0];
		int numRefs=Integer.parseInt(args[1]);
		String outputPrefix=args[2];
		String curatorName=args[3];
		boolean rmDupAuth=Boolean.parseBoolean(args[4]);

		boolean sort=true;
		
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

		log.info("Running "+curatorName);
		TimeUtil.tic();

		Map<Integer,String> curated=curator.curate(graph);

		log.info("Finished curating in "+(TimeUtil.toc()/1000)+" s");

		log.info("Evaluating and writing analysis and results");
		TimeUtil.tic();

		PrintWriter tpw=IOUtil.getPrintWriter(outputPrefix+curatorName+"-tp.txt","utf8",false);
		PrintWriter tnw=IOUtil.getPrintWriter(outputPrefix+curatorName+"-tn.txt","utf8",false);
		PrintWriter fpw=IOUtil.getPrintWriter(outputPrefix+curatorName+"-fp.txt","utf8",false);
		PrintWriter fnw=IOUtil.getPrintWriter(outputPrefix+curatorName+"-fn.txt","utf8",false);

		ExternalCQM[] ecqms=new ExternalCQM[]{new NMI(),new Purity()};
		double[] mvals=new double[ecqms.length];

		int[] stat=new int[4];
		int nontrivial=0;
		int trivial=0;
		for(String name:graph.getVertices()){
			// only evaluate author names
			if(!name.startsWith("N:")) continue;
			List<Integer> instances=new ArrayList<Integer>(graph.getOutEdges(name));
			if(instances.size()<2){
				trivial++;
			}
			else{
				if(!uniform(instances,curated)){
					if(!uniform(instances,truth)){
						stat[TRUE_POS]++;
						tpw.print(getExplanation(graph,name,truth,curated,sort));
						tpw.println();
						tpw.flush();
						// if it's a true positive, we evaluate the actual cluster assignment
						List<String> predIds=getIds(instances,curated);
						List<String> trueIds=getIds(instances,truth);
						for(int j=0;j<ecqms.length;j++){
							LabelSet cLabelSet=new LabelSet();
							LabelSet tLabelSet=new LabelSet();
							List<Label> cLabels=new ArrayList<Label>(instances.size());
							List<Label> tLabels=new ArrayList<Label>(instances.size());
							for(int k=0;k<instances.size();k++){
								cLabels.add(cLabelSet.newLabel(predIds.get(k)));
								tLabels.add(tLabelSet.newLabel(trueIds.get(k)));
							}
							mvals[j]+=ecqms[j].measure(cLabels,tLabels);
						}
					}
					else{
						stat[FALSE_POS]++;
						fpw.print(getExplanation(graph,name,truth,curated,sort));
						fpw.println();
						fpw.flush();
					}
				}
				else{
					if(!uniform(instances,truth)){
						stat[FALSE_NEG]++;
						fnw.print(getExplanation(graph,name,truth,curated,sort));
						fnw.println();
						fnw.flush();
					}
					else{
						stat[TRUE_NEG]++;
						tnw.print(getExplanation(graph,name,truth,curated,sort));
						tnw.println();
						tnw.flush();
					}
				}
				nontrivial++;
			}
		}
		tpw.close();
		tnw.close();
		fpw.close();
		fnw.close();

		log.info("Finished evaluation in "+(TimeUtil.toc()/1000)+" s");
		System.out.println(getIdEvalString(stat,nontrivial,trivial));
		System.out.println(getResEvalString(ecqms,mvals,stat[TRUE_POS]));

	}

	private static final int TRUE_POS=0;  // system thinks it's ambiguous and we know it's ambiguous
	private static final int TRUE_NEG=1;  // system thinks it's not ambiguous and AFAWK it's not
	private static final int FALSE_POS=2; // system thinks it's ambiguous but AFAWK it's not
	private static final int FALSE_NEG=3; // system thinks it's not ambiguous but we know it's ambiguous

	private static String getIdEvalString(int[] eval,int nontrivial,int trivial){
		StringBuilder b=new StringBuilder();
		b.append("Total=").append(nontrivial+trivial).append(" ");
		b.append("Trivial=").append(trivial).append(" ");
		b.append("Non-Trivial=").append(nontrivial).append("\n");
		int count=ArrayUtil.sum(eval);
		b.append("Identified: ").append(nontrivial).append(" ");
		b.append("TP=").append(eval[TRUE_POS]).append(" (").append(FormatUtil.d2((double)eval[TRUE_POS]/count*100)).append("%) ");
		b.append("TN=").append(eval[TRUE_NEG]).append(" (").append(FormatUtil.d2((double)eval[TRUE_NEG]/count*100)).append("%) ");
		b.append("FP=").append(eval[FALSE_POS]).append(" (").append(FormatUtil.d2((double)eval[FALSE_POS]/count*100)).append("%) ");
		b.append("FN=").append(eval[FALSE_NEG]).append(" (").append(FormatUtil.d2((double)eval[FALSE_NEG]/count*100)).append("%) ");
		b.append("Accuracy=").append(FormatUtil.d2(((double)eval[TRUE_POS]+eval[TRUE_NEG])/count*100)).append("%");
		return b.toString();
	}

	private static String getResEvalString(ExternalCQM[] ecqms,double[] mvals,int count){
		StringBuilder b=new StringBuilder();
		b.append("Resolved: ").append(count);
		for(int i=0;i<ecqms.length;i++){
			b.append(" ").append(ecqms[i].getClass().getSimpleName()).append("=").append(FormatUtil.d4(mvals[i]/count));
		}
		return b.toString();
	}

	private static String getExplanation(Graph<String,Integer> graph,String name,Map<Integer,String> truth,Map<Integer,String> pred,boolean sort){
		StringBuilder b=new StringBuilder();
		b.append("= Explanation for \"").append(name).append("\" (").append(graph.degree(name)).append(") =").append("\n");
		Collection<Integer> instances=graph.getOutEdges(name);
		if(sort){
			instances=new ArrayList<Integer>(graph.getOutEdges(name));
			Collections.sort((List<Integer>)instances,new MapUtil.MapComparator<Integer,String>(pred));
		}
		for(Integer instance:instances){
			String paper=graph.getDest(instance);
			b.append("Pred=").append(pred.get(instance)).append(" True=").append(truth.get(instance));
			b.append(" ").append(paper).append(" ").append(graph.getPredecessors(paper)).append("\n");
		}
		return b.toString();
	}

	private static List<String> getIds(List<Integer> instances,Map<Integer,String> map){
		List<String> ids=new ArrayList<String>(instances.size());
		for(Integer instance:instances){
			ids.add(map.get(instance));
		}
		return ids;
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

		if(name.equalsIgnoreCase("base")){
			curator=new ComponentDisambiguator(false);
		}
		else if(name.equalsIgnoreCase("base2")){
			curator=new ComponentDisambiguator(true);
		}
		else if(name.equalsIgnoreCase("modularity")){
			curator=new ModularityDisambiguator();
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
