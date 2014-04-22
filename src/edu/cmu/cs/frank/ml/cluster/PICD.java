package edu.cmu.cs.frank.ml.cluster;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.PlusMult;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.util.Pair;

/**
 * @author Frank Lin
 * 
 * "Deflation" version of PIC
 * 
 */

public class PICD extends PIC{

	protected static Logger log=Logger.getLogger(PICD.class);

	private Algebra alg=new Algebra();

	@Override
	public List<Label> cluster(Dataset dataset){

		log.info("deriving symmetric affinity matrix...");
		DoubleMatrix2D a=Convert.toMatrix(dataset,Convert.Orientation.ROW_INSTANCE,p.dense);
		MatrixUtil.makeSymmetricMax(a);

		long time=System.currentTimeMillis();

		log.info("running PIC projection...");
		projection=project(a);

		log.info("running K-means...");
		KMeans kMeans=new KMeans();
		kMeans.setParams(p.kMeansParams);
		Pair<DoubleMatrix2D,DoubleMatrix2D> result=kMeans.cluster(projection);

		time=System.currentTimeMillis()-time;
		log.info("running time: "+time+" ms.");

		return ClusterUtil.toLabels(result.getB());

	}

	/**
	 * 
	 * @param a - the affinity matrix, row-instance
	 * @return the low-dimensional projection, row-instance
	 */

	@Override
	public DoubleMatrix2D project(DoubleMatrix2D a){

		log.info("creating a normalized matrix...");
		DoubleMatrix2D w=a.copy();
		//MatrixUtil.normalizeSymmetric(w);

		Random random=new Random(p.runSeed);
		
		DoubleMatrix2D proj=new DenseDoubleMatrix2D(a.rows(),p.dimensions);

		for(int i=0;i<p.dimensions;i++){
			// get initial vector
			DoubleMatrix1D initVec=getInitialVector(random,a);
			// power iterate
			DoubleMatrix1D vector=iterate(w,initVec);
			// calculate Rayleigh Quotient
			double rq=alg.mult(vector,alg.mult(w,vector))/alg.mult(vector,vector);
			log.info("Estimated "+i+"-th eigenvalue: "+rq);
			// store first dimension
			proj.viewColumn(i).assign(vector);
			// deflate the matrix
			DoubleMatrix2D b=w.like();
			alg.multOuter(vector,vector,b);
			w.assign(b,PlusMult.minusMult(rq));
		}

		return proj;

	}

}
