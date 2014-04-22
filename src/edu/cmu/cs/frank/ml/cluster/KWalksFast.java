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

public class KWalksFast implements Clusterer{

	protected static Logger log=Logger.getLogger(KWalksFast.class);

	public static enum InitialCenter{
		RANDOM_NODE,
		RANDOM_DISTRIBUTION,
		SPECIFIED_NODES
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

	@Override
	public List<Label> cluster(Dataset dataset){

		log.info("Deriving affinity matrix...");

		DoubleMatrix2D w=Convert.toMatrix(dataset,Convert.Orientation.COLUMN_INSTANCE,p.dense);
		MatrixUtil.makeSymmetricMax(w);
		
		DoubleMatrix1D d=MatrixUtil.getColumnSumVector(w);
		
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
				double total=0.0;
				DoubleMatrix1D rank=new SparseDoubleMatrix1D(w.rows());
				for(int k=0;k<rank.size();k++){
					if(assignment[k]==j){
						rank.setQuick(k,d.getQuick(k));
					}
					total+=d.getQuick(k);
				}
				MatrixUtil.normalizeBy(rank,total);
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