package edu.cmu.cs.frank.ml.util;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Evaluation;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.graph.LabelSticky;
import edu.cmu.cs.frank.ml.graph.WeightedSticky;
import edu.cmu.cs.frank.ml.graph.ssl.LPEdge;
import edu.cmu.cs.frank.ml.graph.ssl.LPGraph;
import edu.cmu.cs.frank.ml.graph.ssl.LPVertex;
import edu.cmu.cs.frank.util.FormatUtil;
import edu.cmu.cs.frank.util.IOUtil;
import edu.cmu.cs.frank.util.RandomUtil;
import edu.cmu.cs.frank.util.Stat;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.graph.Graph;

public class DataUtil{

	static Logger log=Logger.getLogger(DataUtil.class);

	public static Graph<LabelSticky,WeightedSticky> getLCC(Graph<LabelSticky,WeightedSticky> original){

		log.info("Finding connected components...");
		
		WeakComponentClusterer<LabelSticky,WeightedSticky> clusterer=new WeakComponentClusterer<LabelSticky,WeightedSticky>();
		List<Set<LabelSticky>> components=new ArrayList<Set<LabelSticky>>(clusterer.transform(original));
		
		log.info("Found "+components.size()+" connected components.");
		
		Collections.sort(components,Collections.reverseOrder(new Comparator<Set<?>>(){
			@Override
			public int compare(Set<?> a,Set<?> b){
				return a.size()-b.size();
			}
		}));

		Graph<LabelSticky,WeightedSticky> lcc=FilterUtils.createInducedSubgraph(components.get(0),original);
		
		log.info("Largest connected component statistics:");
		log.info("Vertices: "+lcc.getVertexCount());
		log.info("Edges: "+lcc.getEdgeCount());

		return lcc;

	}

	public static void makeSymmetric(LPGraph graph){
		for(LPEdge edge:graph.getEdges()){
			LPVertex source=graph.getSource(edge);
			LPVertex target=graph.getDest(edge);
			graph.addEdge(new LPEdge(graph.getEdgeCount(),edge.weight,source.trueLabel,false),target,source);
		}
	}

	public static String getEdgeLabelingStat(Collection<LPEdge> edges,LPGraph graph){

		List<List<LPEdge>> edgeFactions=new LinkedList<List<LPEdge>>();
		for(int i=0;i<graph.getDimensions();i++){
			edgeFactions.add(new ArrayList<LPEdge>());
		}
		List<LPEdge> intraFactionEdges=new ArrayList<LPEdge>();
		List<LPEdge> interFactionEdges=new ArrayList<LPEdge>();

		for(LPEdge edge:edges){
			edgeFactions.get(edge.trueLabel).add(edge);
			if(edge.isIntraFaction(graph)){
				intraFactionEdges.add(edge);
			}
			else{
				interFactionEdges.add(edge);
			}
		}

		StringBuilder b=new StringBuilder();

		b.append("All Factions: ").append(LPEdge.getAccuracy(edges)).append("\n");
		for(int i=0;i<edgeFactions.size();i++){
			b.append("Faction ").append(i).append(" edges: ").append(LPEdge.getAccuracy(edgeFactions.get(i))).append("\n");
		}
		b.append("Intra-Faction Edges: ").append(LPEdge.getAccuracy(intraFactionEdges)).append("\n");
		b.append("Inter-Faction Edges: ").append(LPEdge.getAccuracy(interFactionEdges)).append("\n");

		return b.toString();

	}

	public static Stat getVertexLabelingStat(Collection<LPVertex> vertices,LPGraph graph){

		List<List<LPVertex>> vertexFactions=new LinkedList<List<LPVertex>>();
		for(int i=0;i<graph.getDimensions();i++){
			vertexFactions.add(new ArrayList<LPVertex>());
		}
		List<LPVertex> inBorderVertices=new ArrayList<LPVertex>();
		List<LPVertex> outBorderVertices=new ArrayList<LPVertex>();
		List<LPVertex> borderVertices=new ArrayList<LPVertex>();
		List<LPVertex> inlandVertices=new ArrayList<LPVertex>();

		for(LPVertex vertex:vertices){
			vertexFactions.get(vertex.trueLabel).add(vertex);
			if(vertex.isInBorderVertex(graph)){
				inBorderVertices.add(vertex);
			}
			if(vertex.isOutBorderVertex(graph)){
				outBorderVertices.add(vertex);
			}
			if(vertex.isBorderVertex(graph)){
				borderVertices.add(vertex);
			}
			else{
				inlandVertices.add(vertex);
			}
		}

		Stat stat=new Stat();
		stat.add("All Factions",LPVertex.getAccuracy(vertices));
		for(int i=0;i<vertexFactions.size();i++){
			stat.add("Faction "+i+" Vertices",LPVertex.getAccuracy(vertexFactions.get(i)));
		}
		stat.add("In-Border Vertices",LPVertex.getAccuracy(inBorderVertices));
		stat.add("Out-Border Vertices",LPVertex.getAccuracy(outBorderVertices));
		stat.add("Border Vertices",LPVertex.getAccuracy(borderVertices));
		stat.add("Inland Vertices",LPVertex.getAccuracy(inlandVertices));

		return stat;

	}

	public static Stat getGraphLabelingStat(LPGraph graph){

		Set<LPVertex> seeds=new HashSet<LPVertex>();
		Set<LPVertex> nonSeeds=new HashSet<LPVertex>();

		for(LPVertex vertex:graph.getVertices()){
			if(vertex.seed){
				seeds.add(vertex);
			}
			else{
				nonSeeds.add(vertex);
			}
		}

		Stat stat=new Stat();
		stat.add("All Vertices ("+graph.getVertexAccuracy()+")",getVertexLabelingStat(graph.getVertices(),graph));
		stat.add("Seed Vertices ("+seeds.size()+")",getVertexLabelingStat(seeds,graph));
		stat.add("Non-Seed Vertices ("+nonSeeds.size()+")",getVertexLabelingStat(nonSeeds,graph));
		stat.add("All Edges ("+graph.getEdgeCount()+")",getEdgeLabelingStat(graph.getEdges(),graph));

		return stat;

	}

	public static Evaluation getGraphLabelingEvaluation(LPGraph graph,String type){

		List<LPVertex> nonSeeds=new ArrayList<LPVertex>();

		for(LPVertex vertex:graph.getVertices()){
			if(!vertex.seed){
				if(type==null||type.equals(vertex.getStuck("Type"))){
					nonSeeds.add(vertex);
				}
			}
		}

		List<Label> truth=new ArrayList<Label>(nonSeeds.size());
		List<Label> prediction=new ArrayList<Label>(nonSeeds.size());
		Map<String,Integer> idMap=new HashMap<String,Integer>(nonSeeds.size());

		for(int i=0;i<nonSeeds.size();i++){
			LPVertex vertex=nonSeeds.get(i);
			truth.add(graph.getLabelSet().newLabel(vertex.trueLabel));
			prediction.add(graph.getLabelSet().newLabel(vertex.predictedLabel));
			idMap.put(vertex.id.toString(),i);
		}

		return Evaluation.calculate(graph.getLabelSet(),truth,prediction,idMap);

	}
	
	public static Evaluation getGraphLabelingEvaluation(LPGraph graph){
		return getGraphLabelingEvaluation(graph,null);
	}

	public static void writeGraphLabelingResult(File file,LPGraph graph,String type){

		PrintWriter writer=IOUtil.getPrintWriter(file,"utf8",true);

		for(LPVertex vertex:graph.getVertices()){
			if(vertex.getStuck("Type").equals(type)){
				writer.print(vertex.id);
				for(int i=0;i<graph.getDimensions();i++){
					writer.print(" ");
					writer.print(graph.getLabelSet().name(i));
					writer.print("=");
					writer.print(vertex.scores[i]);
				}
				writer.println();
			}
		}

		writer.close();

	}

	public static String getGraphStat(LPGraph graph,boolean reportEdgeStat){

		int[] numVertices=new int[graph.getDimensions()];
		int inBorderVertices=0;
		int outBorderVertices=0;
		int borderVertices=0;
		int[] numEdges=new int[graph.getDimensions()];
		int intraClassEdges=0;
		int interClassEdges=0;

		int[] highestDegrees=new int[graph.getDimensions()];

		int badVertices=0;

		int labellessVertices=0;
		int labellessEdges=0; 

		for(LPVertex vertex:graph.getVertices()){
			if(vertex.trueLabel>-1){
				numVertices[vertex.trueLabel]++;
				if(vertex.isInBorderVertex(graph)){
					inBorderVertices++;
				}
				if(vertex.isOutBorderVertex(graph)){
					outBorderVertices++;
				}
				if(vertex.isBorderVertex(graph)){
					borderVertices++;
				}
				if(vertex.degree(graph,vertex.trueLabel)<=graph.degree(vertex)/2){
					badVertices++;
				}
				if(graph.degree(vertex)>highestDegrees[vertex.trueLabel]){
					highestDegrees[vertex.trueLabel]=graph.degree(vertex);
				}
			}
			else{
				labellessVertices++;
			}
		}

		StringBuilder b=new StringBuilder();

		b.append("Total Vertices:").append("\t\t").append(graph.getVertexCount()).append("\n");
		for(int i=0;i<numVertices.length;i++){
			b.append("Class ").append(i).append(" vertices:").append("\t").append(numVertices[i]).append("\n");
		}
		b.append("In-Border Vertices:").append("\t").append(inBorderVertices).append("\n");
		b.append("Out-Border Vertices:").append("\t").append(outBorderVertices).append("\n");
		b.append("Border Vertices:").append("\t").append(borderVertices).append("\n");
		b.append("Labelless Vertices:").append("\t").append(labellessVertices).append("\n");

		b.append("Noisy Vertices:").append("\t\t").append(badVertices).append("\t").append("(").append(FormatUtil.d2((double)badVertices/graph.getVertexCount()*100)).append("%)").append("\n");
		for(int i=0;i<numVertices.length;i++){
			b.append("Class ").append(i).append(" max degree:").append("\t").append(highestDegrees[i]).append("\n");
		}

		if(reportEdgeStat){
			for(LPEdge edge:graph.getEdges()){
				if(edge.trueLabel>-1){
					numEdges[edge.trueLabel]++;
					if(edge.isIntraFaction(graph)){
						intraClassEdges++;
					}
					else{
						interClassEdges++;
					}
				}
				else{
					labellessEdges++;
				}
			}
			b.append("Total Edges:").append("\t\t").append(graph.getEdgeCount()).append("\n");
			for(int i=0;i<numEdges.length;i++){
				b.append("Class ").append(i).append(" edges:").append("\t\t").append(numEdges[i]).append("\n");
			}
			b.append("Intra-Class Edges:").append("\t").append(intraClassEdges).append("\t").append("(").append(FormatUtil.d2((double)intraClassEdges/graph.getEdgeCount()*100)).append("%)").append("\n");
			b.append("Inter-Class Edges:").append("\t").append(interClassEdges).append("\t").append("(").append(FormatUtil.d2((double)interClassEdges/graph.getEdgeCount()*100)).append("%)").append("\n");
			b.append("Labelless Edges:").append("\t").append(labellessEdges).append("\n");
		}

		return b.toString();

	}

	public static String getGraphStat(LPGraph graph){
		return getGraphStat(graph,true);
	}

	public static void augmentWithRandomEdges(LPGraph graph,int numEdges,Random random){
		int numAugmented=0;
		List<LPVertex> vertices=new ArrayList<LPVertex>(graph.getVertices());
		while(numAugmented<numEdges){
			int sourceIndex=random.nextInt(vertices.size());
			int targetIndex=random.nextInt(vertices.size());
			if(sourceIndex!=targetIndex){
				LPVertex source=vertices.get(sourceIndex);
				LPVertex target=vertices.get(targetIndex);
				if(!graph.getSuccessors(source).contains(target)){
					graph.addEdge(new LPEdge(graph.getEdgeCount(),target.trueLabel),source,target);
					numAugmented++;
				}
			}
		}
	}

	public static void augmentWithRandomEdges(LPGraph graph,int numEdges){
		augmentWithRandomEdges(graph,numEdges,new Random());
	}

	public static SortedMap<Integer,Integer> getFeatureCounts(Dataset dataset,int interval){
		SortedMap<Integer,Integer> counts=new TreeMap<Integer,Integer>();
		for(Instance instance:dataset){
			int count=instance.getFeatures().size();
			if(interval>0){
				count=count-count%interval;
			}
			if(!counts.containsKey(count)){
				counts.put(count,0);
			}
			counts.put(count,counts.get(count)+1);
		}
		return counts;
	}

	public static Dataset makeIdealPowerLawDataset(int numClasses,int perClass,double power){
		Dataset dataset=new Dataset();
		for(int i=0;i<numClasses;i++){
			for(int j=0;j<perClass;j++){
				String id=String.valueOf(perClass*i+j);
				Instance instance=dataset.newInstance(id);
				instance.setLabel(dataset.getLabelSet().newLabel(String.valueOf(i)));
				int links=(int)(perClass/((j+1)*power));
				if(j!=0){
					instance.setFeature(String.valueOf(perClass*i),1.0);
				}
				for(int k=1;k<links;k++){
					if(j!=k){
						instance.setFeature(String.valueOf(perClass*i+k),1.0);
					}
				}
			}
		}
		return dataset;
	}

	public static Dataset makeRandomPowerLawDataset(
			int numClasses,
			int perClass,
			double classDistVar,
			double numLinkPower,
			boolean popPref,
			double popPrefPower,
			double noise,
			double badNoise
	){

		Random rand=new Random();

		// Use a Gaussian to decide how many instances per class
		Integer[][] numLinks=new Integer[numClasses][];
		int[] startIndices=new int[numClasses];
		int numNodes=0;
		for(int i=0;i<numClasses;i++){
			double g=rand.nextGaussian()*classDistVar*perClass;
			g=g>perClass?perClass:g;
			numLinks[i]=new Integer[((int)g)+perClass];
			if(i==0){
				startIndices[i]=0;
			}
			else{
				startIndices[i]=startIndices[i-1]+numLinks[i-1].length;
			}
			numNodes+=numLinks[i].length;
		}

		// Randomly pick how many links/features per instance, with at least 1
		for(int i=0;i<numLinks.length;i++){
			for(int j=0;j<numLinks[i].length;j++){
				// Randomly pick how many links to have, but at least 1 and at most perClass
				numLinks[i][j]=RandomUtil.getPowerLawInt(numLinks[i].length,numLinkPower,rand);
			}
			// Sort from most popular to least popular
			Arrays.sort(numLinks[i],Collections.reverseOrder());
		}

		// Keep track of class label
		int[] classLabels=new int[numNodes];

		// Create link matrix
		DoubleMatrix2D linkage=new SparseDoubleMatrix2D(numNodes,numNodes);
		for(int i=0;i<numLinks.length;i++){
			for(int j=0;j<numLinks[i].length;j++){
				int id=startIndices[i]+j;
				classLabels[id]=i;
				int linksNeeded=numLinks[i][j]-linkage.viewRow(id).cardinality();
				if(linksNeeded>0){
					double noisiness;
					if(rand.nextDouble()<=badNoise){
						noisiness=1.0-noise;
					}
					else{
						noisiness=noise;
					}
					for(int k=0;k<linksNeeded;k++){
						// Randomly decide if this link will be noisy
						int c;
						if(rand.nextDouble()>noisiness){
							c=i;
						}
						else{
							c=(i+(rand.nextInt(numClasses-1)+1))%numClasses;
						}
						int link;
						if(popPref){
							// Draw a link biasing toward popular nodes
							link=startIndices[c]+RandomUtil.getPowerLawInt(numLinks[c].length,popPrefPower,rand);
						}
						else{
							// Draw a link uniformly
							link=startIndices[c]+rand.nextInt(numLinks[c].length);
						}
						linkage.setQuick(id,link,1.0);
						linkage.setQuick(link,id,1.0);
					}
				}
			}
		}

		Dataset dataset=new Dataset();
		for(int i=0;i<linkage.columns();i++){
			Instance instance=dataset.newInstance(String.valueOf(i));
			instance.setLabel(dataset.getLabelSet().newLabel(String.valueOf(classLabels[i])));
			for(int j=0;j<linkage.rows();j++){
				if(linkage.getQuick(i,j)!=0.0){
					instance.setFeature(String.valueOf(j),linkage.getQuick(i,j));
				}
			}
		}

		return dataset;
	}

	public static Dataset makeRandomPowerLawDataset2(
			int numClasses,
			int perClass,
			double classDistVar,
			double degScalar,
			double degPower,
			double noise,
			double badNoise
	){

		Random rand=new Random();

		// Use a Gaussian to decide how many instances per class
		Integer[][] numLinks=new Integer[numClasses][];
		// Keeps track of the indexing used for ID
		int[] startIndices=new int[numClasses];
		for(int i=0;i<numClasses;i++){
			double n=rand.nextGaussian()*classDistVar*perClass;
			n=n>perClass?perClass:n;
			numLinks[i]=new Integer[((int)n)+perClass];
			if(i==0){
				startIndices[i]=0;
			}
			else{
				startIndices[i]=startIndices[i-1]+numLinks[i-1].length;
			}
		}

		double total=0.0;

		// Randomly pick how many links/features per instance, with at least 1
		for(int i=0;i<numLinks.length;i++){
			for(int j=0;j<numLinks[i].length;j++){
				// Randomly pick how many links to have, but at least 1 and at most perClass
				numLinks[i][j]=(int)Math.ceil(degScalar*Math.pow(rand.nextDouble(),degPower));
				total+=numLinks[i][j];
			}
			// Sort from most popular to least popular
			Arrays.sort(numLinks[i],Collections.reverseOrder());
		}

		System.out.println("Total: "+total);

		// shuffle bags for random linkage
		List<List<Instance>> bags=new ArrayList<List<Instance>>();

		// create instances, add them to the bag and the dataset
		Dataset dataset=new Dataset();		
		for(int i=0;i<numLinks.length;i++){
			bags.add(new LinkedList<Instance>());
			for(int j=0;j<numLinks[i].length;j++){
				String id=String.valueOf(startIndices[i]+j);
				Instance instance=dataset.newInstance(id);
				instance.setLabel(dataset.getLabelSet().newLabel(String.valueOf(i)));
				dataset.add(instance);
				for(int k=0;k<numLinks[i][j];k++){
					bags.get(i).add(instance);
				}
			}
			Collections.shuffle(bags.get(i),rand);
		}

		// iterate through the instances and get stuff from bags
		for(Instance from:dataset){

			int c=from.getLabel().getBestId();
			int id=Integer.parseInt(from.getId());

			// remove the current instance from its bag
			List<Instance> single=new ArrayList<Instance>();
			single.add(from);
			bags.get(c).removeAll(single);

			// see if this instance is a bad noice - connects mostly to the other side
			double noisiness;
			if(rand.nextDouble()<=badNoise){
				noisiness=1.0-noise;
			}
			else{
				noisiness=noise;
			}

			int links=numLinks[c][id-startIndices[c]];

			for(int k=0;k<links;k++){
				// Randomly decide if this link will be noisy
				int clazz;
				if(rand.nextDouble()>noisiness){
					clazz=c;
				}
				else{
					clazz=(c+(rand.nextInt(numClasses-1)+1))%numClasses;
				}
				if(bags.get(clazz).size()>0){
					Instance to=bags.get(clazz).remove(0);
					from.setFeature(to.getId(),1.0);
				}
			}
		}

		return dataset;
	}

}
