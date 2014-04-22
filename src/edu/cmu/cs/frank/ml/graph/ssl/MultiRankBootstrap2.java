package edu.cmu.cs.frank.ml.graph.ssl;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.Parameters;

public class MultiRankBootstrap2 implements LabelPropagator{

	protected static Logger log=Logger.getLogger(MultiRankBootstrap2.class);
	
	public static class Params extends Parameters{
		
		static final long serialVersionUID=20111017L;

		public LabelPropagator base=new MultiRankSimple(0.85,30);
		public int maxT=100;
		public boolean constantSeedLabels=false;
		public VertexClassifier vertexClassifier=new VertexNeighborRankClassifier(1.0,20.0);
		public EdgeClassifier edgeClassifier=new EdgeEndpointsClassifier();
		
	}
	
	private static enum Mode {EDGE_LABELING,MULTIRANKING,FINISHED};

	private Set<LPEdge> unlabeled;
	private Set<LPEdge> labeled;

	private Mode mode;
	private int t;
	
	private Params p;

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
	
	@Override
	public int getMaxT(){
		return p.maxT;
	}

	private void initialize(){

		log.info("Initializing graph...");

		unlabeled=new HashSet<LPEdge>();
		labeled=new HashSet<LPEdge>();

		for(LPEdge edge:p.base.getGraph().getEdges()){
			edge.alive=false;
			edge.predictedLabel=-1;
			unlabeled.add(edge);
		}

		mode=Mode.EDGE_LABELING;
		t=0;

		log.info("Initialization complete.");

	}

	private void predictVertexLabels(){
		for(LPVertex vertex:p.base.getGraph().getVertices()){
			boolean predictLabel;
			if(p.constantSeedLabels){
				if(vertex.seed){
					predictLabel=false;
				}
				else{
					predictLabel=true;
				}
			}
			else{
				predictLabel=true;
			}
			if(predictLabel){
				vertex.predictedLabel=p.vertexClassifier.predict(p.base.getGraph(),vertex);
				vertex.confident=p.vertexClassifier.isConfident(p.base.getGraph(),vertex);
			}
		}
	}

	private int predictEdgeLabels(){
		int numEdgeChanged=0;
		for(LPEdge edge:p.base.getGraph().getEdges()){
			if(p.edgeClassifier.isConfident(p.base.getGraph(),edge)){
				if(!edge.alive){
					edge.alive=true;
					unlabeled.remove(edge);
					labeled.add(edge);
				}
				int prediction=p.edgeClassifier.predict(p.base.getGraph(),edge);
				if(prediction!=edge.predictedLabel){
					edge.predictedLabel=prediction;
					numEdgeChanged++;
				}
			}
		}
		return numEdgeChanged;
	}

	@Override
	public void run(int numSteps){
		for(int i=0;i<numSteps;i++){
			step();
		}
	}

	@Override
	public void run(){
		long time=System.currentTimeMillis();
		while(t<p.maxT&&!mode.equals(Mode.FINISHED)){
			step();
		}
		if(mode.equals(Mode.FINISHED)){
			log.info("Algorithm converged at t="+t);
		}
		else{
			log.info("Algorithm terminated early at t="+t);
		}
		time=System.currentTimeMillis()-time;
		System.out.println("Bootstrapping finished in "+time+" ms.");
	}

	@Override
	public void step(){
		log.info("Mode: "+mode);
		if(mode.equals(Mode.EDGE_LABELING)){
			int numEdgeChanged=predictEdgeLabels();
			log.info("Changes in edge labeling: "+numEdgeChanged);
			if(numEdgeChanged>0){
				mode=Mode.MULTIRANKING;
			}
			else{
				mode=Mode.FINISHED;
			}
			t++;
		}
		else if(mode.equals(Mode.MULTIRANKING)){
			p.base.resetT();
			p.base.run();
			log.info("MultiRanking finished at t="+p.base.getT());
			p.vertexClassifier.signalGraphUpdate();
			p.edgeClassifier.signalGraphUpdate();
			predictVertexLabels();
			mode=Mode.EDGE_LABELING;
		}
		else if(mode.equals(Mode.FINISHED)){
			log.info("Iterations complete. Nothing more to be done.");
		}
		else{
			log.error("Unrecognized mode: "+mode);
		}

		log.info("---Summary---");
		log.info("Unlabeled Edges: "+unlabeled.size());
		log.info("Labeled Edges: "+labeled.size());
		log.info("Vertex Accuracy: "+p.base.getGraph().getVertexAccuracy());
		log.info("Edge Accuracy: "+p.base.getGraph().getEdgeAccuracy());
		log.info("-------------");

	}

	@Override
	public int getT(){
		return t;
	}

	@Override
	public void reset(){
		p.base.reset();
		initialize();
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

}
