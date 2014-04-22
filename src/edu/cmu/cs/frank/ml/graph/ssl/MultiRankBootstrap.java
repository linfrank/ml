package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.ml.graph.GraphUtil.VertexScoreComparator;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.Parameters;
import edu.uci.ics.jung.algorithms.scoring.PageRank;


public class MultiRankBootstrap implements LabelPropagator{

	protected static Logger log=Logger.getLogger(MultiRankBootstrap.class);
	
	public static enum Seeding{RANDOM,TOP_PAGERANK,BOTTOM_PAGERANK,UNSUPERVISED};

	public static class Params extends Parameters{
		
		static final long serialVersionUID=20111017L;
		
		public LabelPropagator base=new MultiRankSimple(0.85,30);;
		public int numSeeds=5;
		public Seeding seeding=Seeding.TOP_PAGERANK;
		public boolean migrateSeeds=false;
		public double expansionRate=1.0;
		public VertexClassifier vertexClassifier=new VertexSimpleClassifier();
		public int maxT=100;
		public int cycleLimit=5;
		
	}
	
	private static enum Phase {EXPLORATORY,SETTLING};
	private static enum Mode {SEEDING,EXPANSION,MULTIRANKING,EDGE_LABELING,FINISHED};

	private Params p;
	
	private Set<LPVertex> expanded;
	private Set<LPVertex> touched;

	private Phase phase;
	private Mode mode;
	private int numEdgeLabelChange;
	private List<Integer> lastChanges;
	private int t;

	public MultiRankBootstrap(){
		initialize();
		seed();
	}
	
	@Override
	public void setParams(Parameters params){
		this.p=(Params)params;
	}
	
	@Override
	public Parameters getParams(){
		return p;
	}

	@Override
	public LPGraph getGraph(){
		return p.base.getGraph();
	}
	
	@Override
	public void setGraph(LPGraph graph){
		p.base.setGraph(graph);
	}
	
	@Override
	public double getDamper(){
		return p.base.getDamper();
	}

	private void initialize(){

		log.info("Initializing graph...");

		for(LPEdge edge:p.base.getGraph().getEdges()){
			edge.alive=false;
			edge.predictedLabel=-1;
		}

		for(LPVertex vertex:p.base.getGraph().getVertices()){
			vertex.seed=false;
		}

		expanded=new HashSet<LPVertex>();
		touched=new HashSet<LPVertex>();
		phase=Phase.EXPLORATORY;
		mode=Mode.SEEDING;
		numEdgeLabelChange=0;
		t=0;

		log.info("Initialization complete.");

	}
	
	@Override
	public int getMaxT(){
		return p.maxT;
	}

	private void seed(){

		log.info("Seeding graph...");

		if(p.seeding.equals(Seeding.TOP_PAGERANK)||p.seeding.equals(Seeding.BOTTOM_PAGERANK)){
			log.info("Running PageRank for picking seeds...");
			PageRank<LPVertex,LPEdge> pageRank=new PageRank<LPVertex,LPEdge>(p.base.getGraph(),0.15);
			pageRank.evaluate();
			List<LPVertex> seedRanking=new ArrayList<LPVertex>(p.base.getGraph().getVertices());
			Collections.sort(seedRanking,Collections.reverseOrder(new VertexScoreComparator<LPVertex>(pageRank)));
			if(p.seeding.equals(Seeding.BOTTOM_PAGERANK)){
				Collections.reverse(seedRanking);
			}
			for(int i=0;i<getGraph().getDimensions();i++){
				touched.addAll(topN(seedRanking,i,p.numSeeds/getGraph().getDimensions()));
			}
		}
		else if(p.seeding.equals(Seeding.RANDOM)){
			for(int i=0;i<getGraph().getDimensions();i++){
				touched.addAll(randomN(p.base.getGraph(),i,p.numSeeds/getGraph().getDimensions()));
			}
		}
		else if(p.seeding.equals(Seeding.UNSUPERVISED)){
			touched.addAll(randomN(p.base.getGraph(),getGraph().getDimensions()));
		}
		else{
			log.warn("Invalid seeding algorithm, using random seeding instead.");
			for(int i=0;i<getGraph().getDimensions();i++){
				touched.addAll(randomN(p.base.getGraph(),i,p.numSeeds/getGraph().getDimensions()));
			}
		}

		for(LPVertex vertex:touched){
			vertex.seed=true;
		}

		log.info("Seeding complete.");

	}

	private void enlivenEdges(LPVertex vertex,boolean inEdges,boolean outEdges,int assumeLabel){
		Set<Object> candidates=new HashSet<Object>();
		if(inEdges){
			candidates.addAll(p.base.getGraph().getInEdges(vertex));
		}
		if(outEdges){
			candidates.addAll(p.base.getGraph().getOutEdges(vertex));
		}
		for(Object e:candidates){
			LPEdge edge=(LPEdge)e;
			edge.alive=true;
			if(edge.predictedLabel<0){
				if(assumeLabel<0){
					edge.predictedLabel=p.vertexClassifier.predict(p.base.getGraph(),vertex);
				}
				else{
					edge.predictedLabel=assumeLabel;
				}
			}
		}
	}

	private Set<LPVertex> getNeighbors(LPVertex vertex,boolean inNeighbors,boolean outNeighbors){
		Set<LPVertex> neighbors=new HashSet<LPVertex>();
		if(inNeighbors){
			neighbors.addAll(p.base.getGraph().getPredecessors(vertex));
		}
		if(outNeighbors){
			neighbors.addAll(p.base.getGraph().getSuccessors(vertex));
		}
		return neighbors;
	}

	private boolean predictEdgeLabel(LPEdge edge){
		LPVertex source=p.base.getGraph().getSource(edge);
		LPVertex target=p.base.getGraph().getDest(edge);
		int newLabel;
		if(p.vertexClassifier.isConfident(p.base.getGraph(),source)&&!p.vertexClassifier.isConfident(p.base.getGraph(),target)){
			newLabel=p.vertexClassifier.predict(p.base.getGraph(),source);
		}
		else{
			newLabel=p.vertexClassifier.predict(p.base.getGraph(),target);
		}
		if(edge.predictedLabel!=newLabel){
			edge.predictedLabel=newLabel;
			return true;
		}
		else{
			return false;
		}
	}

	private int predictEdgeLabelsSettle(LPVertex vertex){

		// unweighted version
		int[] votes=new int[getGraph().getDimensions()];
		for(LPEdge edge:vertex.getLiveInEdges(p.base.getGraph())){
			LPVertex neighbor=p.base.getGraph().getSource(edge);
			votes[p.vertexClassifier.predict(p.base.getGraph(),neighbor)]++;
		}
		for(LPEdge edge:vertex.getLiveOutEdges(p.base.getGraph())){
			LPVertex neighbor=p.base.getGraph().getDest(edge);
			votes[p.vertexClassifier.predict(p.base.getGraph(),neighbor)]++;
		}

		// weighted version
//		double[] votes=new double[getDimensions()];
//		for(MultiEdge edge:vertex.getLiveInEdges()){
//		MultiVertex neighbor=(MultiVertex)edge.getSource();
//		votes=ArrayUtil.add(votes,neighbor.getRanks());
//		}
//		for(MultiEdge edge:vertex.getLiveOutEdges()){
//		MultiVertex neighbor=(MultiVertex)edge.getDest();
//		votes=ArrayUtil.add(votes,neighbor.getRanks());
//		}

		int inPrediction=ArrayUtil.maxIndex(votes);

		int numChanged=0;
		if((((double)ArrayUtil.max(votes))/ArrayUtil.sum(votes))>0.5){
			for(LPEdge edge:vertex.getLiveInEdges(p.base.getGraph())){
				if(edge.predictedLabel!=inPrediction){
					edge.predictedLabel=inPrediction;
					numChanged++;
				}
			}
		}
		return numChanged;
	}

	private int predictEdgeLabelsExplore(LPVertex vertex){
		int numChanged=0;
		for(LPEdge edge:vertex.getLiveIncidentEdges(p.base.getGraph())){
			if(edge.alive){
				if(predictEdgeLabel(edge)){
					numChanged++;
				}
			}
		}
		return numChanged;
	}

	@Override
	public void run(int numSteps){
		for(int i=0;i<numSteps;i++){
			step();
		}
	}

	@Override
	public void run(){
		if(phase.equals(Phase.EXPLORATORY)){
			while(t<p.maxT&&!phase.equals(Phase.SETTLING)){
				step();
			}
			if(phase.equals(Phase.SETTLING)){
				log.info("Exploratory phase completed at t="+t);
			}
			else{
				log.info("Algorithm terminated early at t="+t);
			}
		}
		else{
			while(t<p.maxT&&!mode.equals(Mode.FINISHED)){
				step();
			}
			if(mode.equals(Mode.FINISHED)){
				log.info("Algorithm converged at t="+t);
			}
			else{
				log.info("Algorithm terminated early at t="+t);
			}
		}
	}

	@Override
	public void step(){
		log.info("Phase: "+phase+" Mode: "+mode);
		if(mode.equals(Mode.SEEDING)){
			List<LPVertex> processing=new ArrayList<LPVertex>(touched);
			if(p.seeding.equals(Seeding.UNSUPERVISED)){
				for(int i=0;i<getGraph().getDimensions();i++){
					enlivenEdges(processing.get(i),true,true,i);
				}
			}
			else{
				for(LPVertex vertex:processing){
					enlivenEdges(vertex,true,true,vertex.trueLabel);
				}
			}
			for(LPVertex vertex:processing){
				Set<LPVertex> neighbors=getNeighbors(vertex,true,true);
				for(LPVertex neighbor:neighbors){
					if(!touched.contains(neighbor)&&!expanded.contains(neighbor)){
						touched.add(neighbor);
					}
				}
				expanded.add(vertex);
				touched.remove(vertex);
			}
			mode=Mode.MULTIRANKING;
		}
		else if(mode.equals(Mode.EXPANSION)){
			List<LPVertex> processing=new ArrayList<LPVertex>(touched);
			Collections.sort(processing,Collections.reverseOrder());
			int expansionSize;
			if(p.expansionRate>0){
				expansionSize=Math.min(processing.size(),(int)(p.expansionRate*expanded.size()));
			}
			else{
				expansionSize=processing.size();
			}
			for(int i=0;i<expansionSize;i++){
				LPVertex vertex=processing.get(i);
				enlivenEdges(vertex,true,true,-1);
				Set<LPVertex> neighbors=getNeighbors(vertex,true,true);
				for(LPVertex neighbor:neighbors){
					if(!touched.contains(neighbor)&&!expanded.contains(neighbor)){
						touched.add(neighbor);
					}
				}
				expanded.add(vertex);
				touched.remove(vertex);
			}
			mode=Mode.MULTIRANKING;
		}
		else if(mode.equals(Mode.MULTIRANKING)){
			p.base.resetT();
			p.base.run();
			for(LPVertex vertex:p.base.getGraph().getVertices()){
				vertex.predictedLabel=p.vertexClassifier.predict(p.base.getGraph(),vertex);
				vertex.confident=p.vertexClassifier.isConfident(p.base.getGraph(),vertex);
			}
			mode=Mode.EDGE_LABELING;
		}
		else if(mode.equals(Mode.EDGE_LABELING)){
			numEdgeLabelChange=0;
			for(LPVertex vertex:p.base.getGraph().getVertices()){
				if(phase.equals(Phase.EXPLORATORY)){
					numEdgeLabelChange+=predictEdgeLabelsExplore(vertex);
				}
				else{
					numEdgeLabelChange+=predictEdgeLabelsSettle(vertex);
				}
			}
			if(numEdgeLabelChange>0){
				log.info("Changes in edge labeling: "+numEdgeLabelChange);
				if(phase.equals(Phase.EXPLORATORY)){
					mode=Mode.MULTIRANKING;
				}
				else{
					if(lastChanges==null){
						lastChanges=new ArrayList<Integer>();
						lastChanges.add(numEdgeLabelChange);
						mode=Mode.MULTIRANKING;
					}
					else{
						lastChanges.add(numEdgeLabelChange);
						if(lastChanges.size()>=p.cycleLimit&&isMonotonic(lastChanges)){
							log.info("Stability reached - cycling changes in edge labels.");
							mode=Mode.FINISHED;
						}
						else{
							if(lastChanges.size()>p.cycleLimit){
								lastChanges.remove(0);
							}
							mode=Mode.MULTIRANKING;
						}
					}
				}
			}
			else{
				log.info("Stability reached - no changes in edge labels.");
				if(expanded.size()<p.base.getGraph().getVertexCount()){
					mode=Mode.EXPANSION;
				}
				else{
					if(phase.equals(Phase.EXPLORATORY)){
						if(p.migrateSeeds){
							phase=Phase.SETTLING;
							mode=Mode.FINISHED;
						}
						else{
							phase=Phase.SETTLING;
							mode=Mode.EDGE_LABELING;
						}
					}
					else{
						mode=Mode.FINISHED;
					}
				}
			}
		}
		else if(mode.equals(Mode.FINISHED)){
			if(p.migrateSeeds){
				log.info("Iterations complete. Migrating seeds.");
				// remember the highest ranking vertices
				List<LPVertex> remember=new ArrayList<LPVertex>();
				for(int i=0;i<getGraph().getDimensions();i++){
					remember.add(p.base.getGraph().getRankedFaction(i).get(0));
				}
				// reset things
				p.base.reset();
				initialize();
				// set and annotate seeds
				touched=new HashSet<LPVertex>(remember);
				for(LPVertex vertex:touched){
					vertex.seed=true;
				}
				// do the seeding mode stuff
				for(int i=0;i<getGraph().getDimensions();i++){
					enlivenEdges(remember.get(i),true,true,i);
				}
				for(LPVertex vertex:remember){
					Set<LPVertex> neighbors=getNeighbors(vertex,true,true);
					for(LPVertex neighbor:neighbors){
						if(!touched.contains(neighbor)&&!expanded.contains(neighbor)){
							touched.add(neighbor);
						}
					}
					expanded.add(vertex);
					touched.remove(vertex);
				}
				// go at it again
				mode=Mode.MULTIRANKING;
			}
			else{
				log.info("Iterations complete. Nothing more to be done.");
			}
		}
		else{
			log.error("Unrecognized mode: "+mode);
		}

		log.info("---Summary---");
		log.info("Vertices Expanded: "+expanded.size());
		log.info("Vertices Touched: "+touched.size());
		log.info("Vertex Accuracy: "+p.base.getGraph().getVertexAccuracy());
		log.info("Edge Accuracy: "+p.base.getGraph().getEdgeAccuracy());
		log.info("-------------");
		t++;
	}

	@Override
	public void reset(){
		p.base.reset();
		initialize();
		seed();
	}
	
	@Override
	public int getT(){
		return t;
	}

	@Override
	public void resetT(){
		p.base.resetT();
		t=0;
	}

	@Override
	public boolean isConverged(){
		return mode.equals(Mode.FINISHED);
	}

	@Override
	public Logger getLogger(){
		return log;
	}

	public LabelPropagator getBase(){
		return p.base;
	}

	private static boolean isMonotonic(List<Integer> list){
		if(list.size()<1){
			return false;
		}
		else if(list.size()==1){
			return true;
		}
		else{
			int num=list.get(0);
			for(int i=1;i<list.size();i++){
				if(list.get(i)!=num){
					return false;
				}
			}
			return true;
		}
	}

	private static List<LPVertex> topN(List<LPVertex> ranking,int faction,int n){
		List<LPVertex> top=new ArrayList<LPVertex>();
		for(int i=0;top.size()<n&&i<ranking.size();i++){
			LPVertex vertex=ranking.get(i);
			if(vertex.trueLabel==faction){
				top.add(vertex);
			}
		}
		return top;
	}

	private static List<LPVertex> randomN(LPGraph graph,int faction,int n){
		List<LPVertex> filtered=new ArrayList<LPVertex>();
		for(LPVertex vertex:graph.getVertices()){
			if(vertex.trueLabel==faction){
				filtered.add(vertex);
			}
		}
		Collections.shuffle(filtered);
		return filtered.subList(0,n);
	}

	private static List<LPVertex> randomN(LPGraph graph,int n){
		List<LPVertex> list=new ArrayList<LPVertex>();
		for(LPVertex vertex:graph.getVertices()){
			list.add(vertex);
		}
		Collections.shuffle(list);
		return list.subList(0,n);
	}

}
