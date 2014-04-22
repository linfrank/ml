package edu.cmu.cs.frank.ml.cluster;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.cluster.PIC.ConvergeCheck;
import edu.cmu.cs.frank.ml.cluster.PIC.InitialVector;
import edu.cmu.cs.frank.ml.cluster.PIC.VectorNormalizer;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.util.Pair;
import edu.cmu.cs.frank.util.Parameters;

/**
 * @author Frank Lin
 * 
 * "Orthogonal" version of PIC
 * 
 */

public class PICO implements ProjectionClusterer{

	protected static Logger log=Logger.getLogger(PICO.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20080512L;

		public InitialVector initialVector=InitialVector.RandomReal;
		public ConvergeCheck convergeCheck=ConvergeCheck.Accel;
		public VectorNormalizer vectorNormalizer=VectorNormalizer.Sum;
		public double convergence=0.0001;
		public int dimensions=2;
		public int maxT=1000;
		public boolean fixedRunSeed=false;
		public int runSeed=0;
		public KMeans.Params kMeansParams=new KMeans.Params();
		public boolean dense=false;

	}

	private Params p;

	@Override
	public void setParams(Parameters params){
		this.p=(Params)params;
	}

	@Override
	public Params getParams(){
		return p;
	}

	private DoubleMatrix2D projection;

	@Override
	public DoubleMatrix2D getProjection(){
		return projection;
	}

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

	public DoubleMatrix2D project(DoubleMatrix2D a){

		log.info("transforming affinity matrix to transition matrix...");
		for(int i=0;i<a.rows();i++){
			DoubleMatrix1D row=a.viewRow(i);
			double sum=a.viewRow(i).zSum();
			row.assign(Functions.div(sum));
		}

		DoubleMatrix2D proj=new DenseDoubleMatrix2D(a.rows(),p.dimensions);

		DoubleMatrix1D initVec;
		if(p.initialVector.equals(InitialVector.Degree)){
			log.info("creating initial degree vector...");
			initVec=MatrixUtil.getDegreeVector(a);
		}
		else if(p.initialVector.equals(InitialVector.RandomInt)){
			log.info("creating initial random vector...");
			if(p.fixedRunSeed){
				initVec=MatrixUtil.getRandomInt(a.rows(),1,Integer.MAX_VALUE,new Random(p.runSeed));
			}
			else{
				initVec=MatrixUtil.getRandomInt(a.rows(),1,Integer.MAX_VALUE);
			}
		}
		else{
			log.info("creating initial random vector...");
			if(p.fixedRunSeed){
				initVec=MatrixUtil.getRandomReal(a.rows(),new Random(p.runSeed));
			}
			else{
				initVec=MatrixUtil.getRandomReal(a.rows());
			}
		}
		
		for(int i=0;i<p.dimensions;i++){
			DoubleMatrix1D v=iterate(a,initVec);
			proj.viewColumn(i).assign(v);
			double min=MatrixUtil.getMin(v);
			double max=MatrixUtil.getMax(v);
			double mid=(max-min)/2+min;
			for(int j=0;j<v.size();j++){
				initVec.setQuick(i,Math.abs(v.getQuick(i)-mid));
			}
		}

		return proj;

	}

	public DoubleMatrix1D iterate(DoubleMatrix2D matrix,DoubleMatrix1D vector){

		// set up convergence check vars
		boolean converged=false;
		boolean checkSpeed=p.convergeCheck.equals(ConvergeCheck.Speed);
		boolean checkAccel=p.convergeCheck.equals(ConvergeCheck.Accel);
		DoubleMatrix1D prevVec=checkSpeed||checkAccel?vector.like():null;
		DoubleMatrix1D prevDelta=checkAccel?vector.like():null;

		double threshold=p.convergence/vector.size();
		log.info("adjusted convergence threshold: "+threshold);

		int t=0;
		while(t<p.maxT&&!converged){

			// save previous vector
			prevVec=vector;

			// multiply
			vector=alg.mult(matrix,vector);

			// normalize vector
			if(p.vectorNormalizer.equals(VectorNormalizer.Max)){
				MatrixUtil.normalizeBy(vector,MatrixUtil.getMax(vector));
			}
			else{
				MatrixUtil.normalize(vector);
			}

			// check convergence
			if(checkSpeed||checkAccel){
				DoubleMatrix1D delta=MatrixUtil.getAbsDiff(vector,prevVec);
				if(checkSpeed){
					converged=MatrixUtil.getMax(delta)<threshold;
				}
				else if(checkAccel){
					DoubleMatrix1D accel=MatrixUtil.getAbsDiff(delta,prevDelta);
					converged=MatrixUtil.getMax(accel)<threshold;
					prevDelta=delta;
				}
			}

			t++;
		}

		log.info("Finished in "+t+" iterations.");

		return vector;

	}

}
