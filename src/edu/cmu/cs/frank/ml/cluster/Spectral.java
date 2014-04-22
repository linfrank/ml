package edu.cmu.cs.frank.ml.cluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.jet.math.Functions;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.util.MemCache;
import edu.cmu.cs.frank.util.Pair;
import edu.cmu.cs.frank.util.Parameters;

/**
 * @author Frank Lin
 * 
 * Spectral Clustering using different graph Laplacians
 * 
 * See:
 * A. Ng, M. Jordan, and Y. Weiss. On spectral clustering: 
 * Analysis and an algorithm. NIPS 2001.
 * 
 * U. von Luxburg. A Tutorial on Spectral Clustering.
 * Statistics and Computing 17(4): 395-416, 2007.
 * 
 */

public class Spectral implements SpectralClusterer{

	protected static Logger log=Logger.getLogger(Spectral.class);

	public static enum Laplacian{
		NormalizedRW,
		NormalizedSYM,
		Unnormalized,
	}

	public static class Params extends Parameters{

		static final long serialVersionUID=20080512L;

		public Laplacian laplacian=Laplacian.NormalizedRW;
		public double laplacianAlpha=1.0;
		public int eigenvectors=2;
		public double eigenweight=0.0;
		public boolean dense=false;
		public boolean cache=true;
		public KMeans.Params kMeansParams=new KMeans.Params();

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
		
		long time=System.currentTimeMillis();

		String key=toString()+p+dataset.size();

		if(p.cache){
			log.info("Checking for cached indicator...");
			if(MemCache.contains(key)){
				log.info("Loading cached indicator...");
				indicator=(DoubleMatrix2D)MemCache.get(key);
			}
			else{
				log.info("No cached indicator...");
				indicator=null;
			}
		}

		if(indicator==null){

			log.info("Deriving affinity matrix...");
			DoubleMatrix2D w=Convert.toMatrix(dataset,Convert.Orientation.COLUMN_INSTANCE,p.dense);
			// Spectral methods work only on symmetric matrices
			MatrixUtil.makeSymmetricMax(w); 

			log.info("Deriving graph laplacian...");
			DoubleMatrix2D l;
			if(p.laplacian.equals(Laplacian.NormalizedRW)){
				l=MatrixUtil.getNormalizedLaplacianMatrix1(w,p.laplacianAlpha);
			}
			else if(p.laplacian.equals(Laplacian.NormalizedSYM)){
				l=MatrixUtil.getNormalizedLaplacianMatrix2(w,p.laplacianAlpha);
			}
			else if(p.laplacian.equals(Laplacian.Unnormalized)){
				l=MatrixUtil.getUnnormalizedLaplacianMatrix(w);
			}
			else{
				log.error("Unrecognized graph laplacian: "+p.laplacian);
				l=null;
			}

			time=System.currentTimeMillis();
			
			log.info("Running eigenvalue decomposition...");
			EigenvalueDecomposition decomp=new EigenvalueDecomposition(l);
			DoubleMatrix1D eigenvalues=decomp.getRealEigenvalues();
			sortedEigenvalues=eigenvalues.viewSorted();
			
			int numEig=Math.min(p.eigenvectors,dataset.size());

			List<Integer> eigenvalueIndices=getSmallestEigenIndices(eigenvalues,numEig);

			for(int i:eigenvalueIndices){
				log.info("Using eigenvalue "+i+" = "+eigenvalues.get(i));
			}

			log.info("Getting "+numEig+" indicator vectors...");
			indicator=new DenseDoubleMatrix2D(dataset.size(),numEig);
			for(int i=0;i<numEig;i++){
				indicator.viewColumn(i).assign(decomp.getV().viewColumn(eigenvalueIndices.get(i)));
				if(p.eigenweight!=0.0){
					indicator.viewColumn(i).assign(Functions.mult(Math.pow(1.0-sortedEigenvalues.getQuick(i),p.eigenweight)));
				}
			}
			if(p.laplacian.equals(Laplacian.NormalizedRW)){
				indicator=indicator.viewPart(0,1,indicator.rows(),indicator.columns()-1);
			}

			if(p.laplacian.equals(Laplacian.NormalizedSYM)){
				log.info("Renormalizing indicator vectors...");
				for(int i=0;i<indicator.rows();i++){
					double z=0.0;
					for(int j=0;j<indicator.columns();j++){
						z+=Math.pow(indicator.getQuick(i,j),2);
					}
					z=Math.sqrt(z);
					for(int j=0;j<indicator.columns();j++){
						indicator.set(i,j,indicator.getQuick(i,j)/z);
					}
				}
			}

			if(p.cache){
				MemCache.cache(key,indicator);
			}

		}

		log.info("Running K-means...");
		KMeans kMeans=new KMeans();
		kMeans.setParams(p.kMeansParams);
		Pair<DoubleMatrix2D,DoubleMatrix2D> result=kMeans.cluster(indicator);
		
		time=System.currentTimeMillis()-time;
		log.info("Core running time: "+time+" ms.");

		return ClusterUtil.toLabels(result.getB());

	}

	@Override
	public DoubleMatrix2D getProjection(){
		return indicator;
	}
	
	@Override
	public DoubleMatrix1D getSortedEigenvalues(){
		return sortedEigenvalues;
	}

	private static List<Integer> getSmallestEigenIndices(DoubleMatrix1D values,int k){

		SortedMap<Double,Integer> orth=new TreeMap<Double,Integer>();
		for(int i=0;i<values.size();i++){
			orth.put(values.get(i),i);
		}

		List<Integer> indices=new ArrayList<Integer>();
		for(Iterator<Double> it=orth.keySet().iterator();it.hasNext()&&indices.size()<k;){
			indices.add(orth.get(it.next()));
		}
		return indices;

	}

}
