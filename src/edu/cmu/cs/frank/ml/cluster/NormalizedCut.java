package edu.cmu.cs.frank.ml.cluster;

import java.util.List;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.util.Parameters;

/**
 * @author Frank Lin
 * 
 * According to the Shi & Malik 2000 paper, currently only supports two clusters
 * (no recursive cuts)
 */

public class NormalizedCut implements SpectralClusterer{
	
	protected static Logger log=Logger.getLogger(NormalizedCut.class);
	
	public static class Params extends Parameters{
		
		static final long serialVersionUID=20080512L;

		public double threshold=0.0;
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
	
	private DoubleMatrix2D indicator;
	
	private DoubleMatrix1D sortedEigenvalues; 

	@Override
	public List<Label> cluster(Dataset dataset){
		
		log.info("Deriving affinity matrix...");
		DoubleMatrix2D w=Convert.toMatrix(dataset,Convert.Orientation.COLUMN_INSTANCE,p.dense);
		
		log.info("Deriving graph laplacian...");
		DoubleMatrix2D l=MatrixUtil.getNormalizedLaplacianMatrix2(w);
		
		log.info("Running eigenvalue decomposition...");
		EigenvalueDecomposition decomp=new EigenvalueDecomposition(l);
		DoubleMatrix1D eigenvalues=decomp.getRealEigenvalues();
		
		log.info("Getting the indicator vector...");
		int v=MatrixUtil.getNthSmallestValueIndex(eigenvalues,1);
		indicator=decomp.getV().viewPart(0,v,dataset.size(),1);
		
		// sort eigenvalues for viewing
		sortedEigenvalues=eigenvalues.viewSorted();
		
		log.info("Assigning clusters...");
		
		// construct the Cluster objects - currently only making two clusters
		DoubleMatrix2D scores=new DenseDoubleMatrix2D(dataset.size(),2);
		for(int i=0;i<dataset.size();i++){
			scores.setQuick(i,0,indicator.getQuick(i,0)-p.threshold);
			scores.setQuick(i,1,p.threshold-indicator.getQuick(i,0));
		}
		
		return ClusterUtil.toLabels(scores);
		
	}
	
	@Override
	public DoubleMatrix2D getProjection(){
		return indicator;
	}
	
	@Override
	public DoubleMatrix1D getSortedEigenvalues(){
		return sortedEigenvalues; 
	}

}
