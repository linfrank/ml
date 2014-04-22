package edu.cmu.cs.frank.ml.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.Parameters;
import edu.cmu.cs.frank.util.RandomUtil;

/**
 * Very much like K-means, but with distributions over nodes as cluster centers.
 * 
 * @author Frank Lin
 */

public class KWalks implements Clusterer{

	protected static Logger log=Logger.getLogger(KWalks.class);

	public static enum InitialCenter{
		RANDOM_NODE,
		RANDOM_DISTRIBUTION,
		PAGERANK_NODE,
		SPECIFIED_NODES,
		COMPETING_WALKS
	}

	public static enum Center{
		NODE,
		DISTRIBUTION
	}

	public static class Params extends Parameters{

		static final long serialVersionUID=20080513L;

		public int k=2;
		public InitialCenter initialCenter=InitialCenter.RANDOM_NODE;
		public Center center=Center.DISTRIBUTION;
		public int pageRankT=100;
		public double teleProb=0.15;
		public int randomWalkT=100;
		public double restartProb=0.15;
		public double restartReg=0.0;
		public int maxRounds=50;
		public double convergence=0.0001;
		public boolean dense=false;

	}

	private Params p;

	@Override
	public void setParams(Parameters params){
		this.p=(Params)params;
	}

	@Override
	public Parameters getParams(){
		return p;
	}
	
	private List<List<String>> initNodes;
	
	public void setInitNodes(List<List<String>> initNodes){
		this.initNodes=initNodes;
	}

	private static DoubleMatrix1D getUniformVector(int size){
		DoubleMatrix1D uniform=new DenseDoubleMatrix1D(size);
		uniform.assign(1.0/size);
		return uniform;
	}

	private static DoubleMatrix1D getStartVector(int size,int startIndex){
		DoubleMatrix1D start=new DenseDoubleMatrix1D(size);
		start.setQuick(startIndex,1.0);
		return start;
	}

	private static DoubleMatrix1D getRandomStartVector(int size){
		Random random=new Random();
		DoubleMatrix1D start=new DenseDoubleMatrix1D(size);
		for(int i=0;i<start.size();i++){
			start.setQuick(i,random.nextDouble());
		}
		MatrixUtil.normalize(start);
		return start;
	}

	private static void regularize(DoubleMatrix1D v,double reg){
		if(reg==1.0){
			v.assign(1.0/v.size());
		}
		else if(reg>0){
			double weight=1.0-reg;
			double uniform=1.0/v.size();
			for(int i=0;i<v.size();i++){
				v.setQuick(i,v.getQuick(i)*weight+uniform*reg);
			}
		}
	}

	private static int[] getClusterAssignment(DoubleMatrix1D[] walks){
		int[] assignment=new int[walks[0].size()];
		for(int i=0;i<assignment.length;i++){
			double[] scores=new double[walks.length];
			for(int j=0;j<scores.length;j++){
				scores[j]=walks[j].get(i);
			}
			assignment[i]=ArrayUtil.maxIndex(scores);
		}
		return assignment;
	}

	private boolean converged(DoubleMatrix1D rank,DoubleMatrix1D prevRank){
		return MatrixUtil.getEuclideanDistance(rank,prevRank)<p.convergence;
	}

	private boolean converged(DoubleMatrix1D[] ranks,DoubleMatrix1D[] prevRanks){
		for(int i=0;i<ranks.length;i++){
			if(ranks[i]==null||prevRanks[i]==null){
				return false;
			}
			if(!converged(ranks[i],prevRanks[i])){
				return false;
			}
		}
		return true;
	}

	private int[] getCompetingWalkCenters(int k,DoubleMatrix2D w){

		// get PageRank score
		DoubleMatrix1D u=getUniformVector(w.rows());
		DoubleMatrix1D pr=MatrixUtil.getPageRankVector(w,u,p.teleProb,p.pageRankT,p.convergence);

		// the first center is the node with the highest PageRank score
		int[] centers=new int[k];
		centers[0]=MatrixUtil.getMaxIndex(pr);

		for(int i=1;i<k;i++){
			// create a start vector and fill it according to centers and normalize
			DoubleMatrix1D start=new SparseDoubleMatrix1D(u.size());
			for(int j=0;j<i;j++){
				start.setQuick(centers[j],1.0);
			}
			MatrixUtil.normalize(start);
			// the next center is the node with highest PR and lowest competing RW score
			DoubleMatrix1D walk=MatrixUtil.getPageRankVector(w,start,p.restartProb,p.randomWalkT,p.convergence);
			DoubleMatrix1D indicator=pr.copy().assign(walk,MatrixUtil.SUBTRACT);
			centers[i]=MatrixUtil.getMaxIndex(indicator);
		}

		// return centers
		return centers;
	}

	private static void zero(DoubleMatrix2D m,int index){
		for(int i=0;i<m.rows();i++){
			m.setQuick(index,i,0.0);
			m.setQuick(i,index,0.0);
		}
	}

	private static DoubleMatrix2D getMaskedMatrix(DoubleMatrix2D original,int[] assignment,int mask){
		DoubleMatrix2D masked=original.copy();
		for(int i=0;i<assignment.length;i++){
			if(assignment[i]!=mask){
				zero(masked,i);
			}
		}
		return masked;
	}

	private static DoubleMatrix1D getMaskedUniformVector(int size,int[] assignment,int mask){
		DoubleMatrix1D masked=new DenseDoubleMatrix1D(size);
		for(int i=0;i<assignment.length;i++){
			if(assignment[i]==mask){
				masked.setQuick(i,1.0);
			}
		}
		MatrixUtil.normalize(masked);
		return masked;
	}

	@Override
	public List<Label> cluster(Dataset dataset){

		log.info("Deriving affinity matrix...");

		DoubleMatrix2D w=Convert.toMatrix(dataset,Convert.Orientation.COLUMN_INSTANCE,p.dense);
		MatrixUtil.makeSymmetricMax(w);
		MatrixUtil.normalizeColumns(w);

		log.info("Calculating "+p.k+" initial centers...");
		
		DoubleMatrix1D[] centers=new DoubleMatrix1D[p.k];
		if(p.initialCenter.equals(InitialCenter.RANDOM_NODE)){
			log.info("Initializing random nodes...");
			List<Integer> nodes=RandomUtil.drawInteger(w.rows(),p.k);
			for(int i=0;i<p.k;i++){
				centers[i]=getStartVector(w.rows(),nodes.get(i));
			}
		}
		else if(p.initialCenter.equals(InitialCenter.RANDOM_DISTRIBUTION)){
			log.info("Initializing random distributions...");
			for(int i=0;i<p.k;i++){
				centers[i]=getRandomStartVector(w.rows());
			}
		}
		else if(p.initialCenter.equals(InitialCenter.PAGERANK_NODE)){
			log.info("Initializing PageRank nodes...");
			DoubleMatrix1D u=getUniformVector(w.rows());
			DoubleMatrix1D pr=MatrixUtil.getPageRankVector(w,u,p.teleProb,p.pageRankT,p.convergence);
			for(int i=0;i<p.k;i++){
				centers[i]=getStartVector(w.rows(),MatrixUtil.getNthLargestValueIndex(pr,i));
			}
		}
		else if(p.initialCenter.equals(InitialCenter.SPECIFIED_NODES)){
			log.info("Initializing specified nodes...");
			// build a quick index
			Map<String,Integer> idIndexMap=new HashMap<String,Integer>();
			for(int i=0;i<dataset.size();i++){
				idIndexMap.put(dataset.getInstance(i).getId(),i);
			}
			for(int i=0;i<p.k;i++){
				centers[i]=new DenseDoubleMatrix1D(dataset.getFeatureSet().size());
				List<String> ids=initNodes.get(i);
				for(String id:ids){
					centers[i].setQuick(idIndexMap.get(id),1.0/ids.size());
				}
			}
		}
		else if(p.initialCenter.equals(InitialCenter.COMPETING_WALKS)){
			log.info("Initializing competing walk nodes...");
			int[] centerNodes=getCompetingWalkCenters(p.k,w);
			for(int i=0;i<p.k;i++){
				centers[i]=getStartVector(w.rows(),centerNodes[i]);
			}
		}
		else{
			log.error("Unrecognized initial center method.");
			centers=null;
		}

		log.info("Running k-walks...");

		DoubleMatrix1D[] prevCenters=new DoubleMatrix1D[p.k];		
		DoubleMatrix1D[] walks=new DoubleMatrix1D[p.k];

		for(int i=0;i<p.maxRounds&&!converged(centers,prevCenters);i++){

			log.info("Iteration "+i+":");

			log.info("Central Nodes:");
			for(int j=0;j<centers.length;j++){
				int index=MatrixUtil.getMaxIndex(centers[j]);
				log.info(j+"th Center: Index="+index+" ID="+dataset.getInstance(index).getId());
			}

			for(int j=0;j<p.k;j++){
				log.info("Walking from "+j+"th center...");
				DoubleMatrix1D start;
				if(p.center.equals(Center.NODE)){
					start=getStartVector(w.rows(),MatrixUtil.getMaxIndex(centers[j]));
				}
				else if(p.center.equals(Center.DISTRIBUTION)){
					start=centers[j];
				}
				else{
					log.error("Unrecognized center definition.");
					start=null;
				}
				regularize(start,p.restartReg);
				walks[j]=MatrixUtil.getPageRankVector(w,start,p.restartProb,p.randomWalkT,p.convergence);
			}

			log.info("Assigning Clusters...");

			int[] assignment=getClusterAssignment(walks);

			log.info("Recentering...");

			for(int j=0;j<p.k;j++){
				prevCenters[j]=centers[j];
				DoubleMatrix2D masked=getMaskedMatrix(w,assignment,j);
				DoubleMatrix1D u=getMaskedUniformVector(w.rows(),assignment,j);
				DoubleMatrix1D rank=MatrixUtil.getPageRankVector(masked,u,p.teleProb,p.pageRankT,p.convergence);
				centers[j]=rank;
			}

		}

		log.info("Creating clusters...");

		DoubleMatrix2D indicator=new DenseDoubleMatrix2D(dataset.size(),p.k);
		for(int i=0;i<walks.length;i++){
			for(int j=0;j<walks[i].size();j++){
				indicator.setQuick(j,i,walks[i].getQuick(j));
			}
		}

		return ClusterUtil.toLabels(indicator);

	}

}