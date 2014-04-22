package edu.cmu.cs.frank.ml.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

/**
 * Like KWalks, but tries to figure out K on its own.
 * 
 * @author Frank Lin
 */

public class KWalksExp implements Clusterer{

	protected static Logger log=Logger.getLogger(KWalksExp.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20080918L;

		public double threshold=0.0001;
		public int pageRankT=100;
		public double teleProb=0.15;
		public int randomWalkT=100;
		public double restartProb=0.15;
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
	
	private static DoubleMatrix1D getUniformVector(int size){
		DoubleMatrix1D uniform=new DenseDoubleMatrix1D(size);
		uniform.assign(1.0/size);
		return uniform;
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
	
	private List<DoubleMatrix1D> spawn(List<DoubleMatrix1D> ranks,int numInstances){
		List<DoubleMatrix1D> newRanks=new ArrayList<DoubleMatrix1D>();
		if(ranks.size()==0){
			newRanks.add(getUniformVector(numInstances));
		}
		else{
			for(int i=0;i<ranks.size();i++){
				newRanks.add(ranks.get(i));
			}
			DoubleMatrix1D newRank;
			if(p.dense){
				newRank=new DenseDoubleMatrix1D(numInstances);
			}
			else{
				newRank=new SparseDoubleMatrix1D(numInstances);
			}
			for(int i=0;i<numInstances;i++){
				double sum=0.0;
				for(int j=0;j<ranks.size();j++){
					sum+=ranks.get(j).getQuick(i);
				}
				newRank.setQuick(i,ranks.size()-sum);
			}
			int index=MatrixUtil.getMaxIndex(newRank);
			newRank.assign(0);
			newRank.set(index,1.0);
			newRanks.add(newRank);
		}
		return newRanks;
	}
	
	private List<DoubleMatrix1D> subcluster(final DoubleMatrix2D w,final List<DoubleMatrix1D> ranksList){
		
		DoubleMatrix1D[] ranks=ranksList.toArray(new DoubleMatrix1D[ranksList.size()]);
		DoubleMatrix1D[] prevRanks=new DoubleMatrix1D[ranksList.size()];
		DoubleMatrix1D[] walks=new DoubleMatrix1D[ranksList.size()];
		
		for(int i=0;i<p.maxRounds&&!converged(ranks,prevRanks);i++){

			log.info("Subiteration "+i+":");

			for(int j=0;j<ranks.length;j++){
				log.info("Walking cluster "+j+"...");
				walks[j]=MatrixUtil.getPageRankVector(w,ranks[j],p.restartProb,p.randomWalkT,p.convergence);
			}

			log.info("Assigning Clusters...");

			int[] assignment=getClusterAssignment(walks);
			
			log.info(Arrays.toString(assignment));

			log.info("Recentering...");

			for(int j=0;j<ranks.length;j++){
				prevRanks[j]=ranks[j];
				DoubleMatrix2D masked=getMaskedMatrix(w,assignment,j);
				DoubleMatrix1D u=getMaskedUniformVector(w.rows(),assignment,j);
				DoubleMatrix1D rank=MatrixUtil.getPageRankVector(masked,u,p.teleProb,p.pageRankT,p.convergence);
				ranks[j]=rank;
				log.info(rank);
			}
			
			log.info("Subiteration "+i+" done.");

		}
		
		return ArrayUtil.toList(ranks);
		
	}
	
	private boolean stop(List<DoubleMatrix1D> ranks){
		for(int i=0;i<ranks.size();i++){
			for(int j=i+1;j<ranks.size();j++){
				double dist=MatrixUtil.getEuclideanDistance(ranks.get(i),ranks.get(j));
				if(dist<p.threshold){
					log.info("Distance below threshold: "+dist);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public List<Label> cluster(Dataset dataset){

		log.info("Deriving affinity matrix...");

		DoubleMatrix2D w=Convert.toMatrix(dataset,Convert.Orientation.COLUMN_INSTANCE,p.dense);
		MatrixUtil.normalizeColumns(w);
		
		List<DoubleMatrix1D> ranks;
		List<DoubleMatrix1D> candidates=new ArrayList<DoubleMatrix1D>();

		do{
			
			ranks=candidates;
			
			log.info("Current K="+ranks.size());
			candidates=spawn(ranks,dataset.size());
			
			log.info("With new candidate:");
			for(DoubleMatrix1D candidate:candidates){
				log.info(candidate);
			}
			
			log.info("Trying K="+candidates.size());
			candidates=subcluster(w,candidates);
			
			log.info("With new candidate after walking:");
			for(DoubleMatrix1D candidate:candidates){
				log.info(candidate);
			}
			
		}
		while(!stop(candidates));

		log.info("Creating clusters...");

		DoubleMatrix2D indicator=new DenseDoubleMatrix2D(dataset.size(),ranks.size());
		for(int i=0;i<ranks.size();i++){
			for(int j=0;j<ranks.get(i).size();j++){
				indicator.setQuick(j,i,ranks.get(i).getQuick(j));
			}
		}
		
		return ClusterUtil.toLabels(indicator);

	}

}