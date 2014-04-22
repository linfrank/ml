package edu.cmu.cs.frank.ml.cluster;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cern.colt.function.DoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.util.Pair;
import edu.cmu.cs.frank.util.Parameters;

/**
 * @author Frank Lin
 * 
 * Power Iteration Clustering
 * 
 * Uses the pre-convergent values of the 1st eigenvector to create a
 * low-dimensional (often just 1) embedding of the data, much like spectral
 * clustering, then cluster the embedded data points using something like
 * k-means.
 * 
 */

public class PIC implements ProjectionClusterer{

	protected static Logger log=Logger.getLogger(PICS.class);

	public static enum InitialVector{
		RandomReal,
		RandomInt,
		Degree
	}

	public static enum ConvergeCheck{
		Accel,
		Speed,
		None
	}

	public static enum VectorNormalizer{
		Sum,
		Max
	}

	public static class Params extends Parameters{

		static final long serialVersionUID=20080512L;

		public InitialVector initialVector=InitialVector.Degree;
		public ConvergeCheck convergeCheck=ConvergeCheck.Accel;
		public VectorNormalizer vectorNormalizer=VectorNormalizer.Sum;
		public double convergence=0.000001;
		public int dimensions=1;
		public boolean stretchDims=true;
		public boolean repel=false;
		public int maxT=1000;
		public boolean fixedRunSeed=false;
		public int runSeed=0;
		public KMeans.Params kMeansParams=new KMeans.Params();
		public boolean dense=false;

	}

	protected Params p;

	@Override
	public void setParams(Parameters params){
		this.p=(Params)params;
	}

	@Override
	public Params getParams(){
		return p;
	}

	protected DoubleMatrix2D projection;

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

		log.info("creating a transition matrix...");
		DoubleMatrix2D w=a.copy();
		MatrixUtil.normalizeRows(w);

		// store projection vectors in a matrix
		DoubleMatrix2D proj=new DenseDoubleMatrix2D(w.rows(),p.dimensions);

		// random for initial vectors
		Random random=new Random(p.runSeed);

		// project
		for(int i=0;i<p.dimensions;i++){
			// create initial vector
			DoubleMatrix1D initVec=getInitialVector(random,a);
			// power iterate
			proj.viewColumn(i).assign(iterate(w,initVec));
		}

		return proj;

	}

	public DoubleMatrix1D getInitialVector(Random random,DoubleMatrix2D affinity){
		if(p.initialVector.equals(InitialVector.Degree)){
			log.info("creating initial degree vector...");
			return MatrixUtil.getDegreeVector(affinity);
		}
		else if(p.initialVector.equals(InitialVector.RandomReal)){
			log.info("creating initial random real vector...");
			if(p.fixedRunSeed){
				return MatrixUtil.getRandomReal(affinity.rows(),random);
			}
			else{
				return MatrixUtil.getRandomReal(affinity.rows());
			}
		}
		else{
			log.info("creating initial random integer vector...");
			if(p.fixedRunSeed){
				return MatrixUtil.getRandomInt(affinity.rows(),1,Integer.MAX_VALUE,random);
			}
			else{
				return MatrixUtil.getRandomInt(affinity.rows(),1,Integer.MAX_VALUE);
			}
		}
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

			//testing...
			if(p.repel){
				vector=repel(prevVec,vector);
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

		if(p.stretchDims){
			final double min=MatrixUtil.getMin(vector);
			vector.assign(new DoubleFunction(){
				@Override
				public double apply(double a){
					return a-min;
				}
			});
			MatrixUtil.normalizeBy(vector,MatrixUtil.getMax(vector));
		}

		return vector;

	}

	// trying a crazy idea here...
	private DoubleMatrix1D repel(DoubleMatrix1D prev,DoubleMatrix1D curr){
		
		double range=MatrixUtil.getMax(prev)-MatrixUtil.getMin(prev);
		
		double minF=range/prev.size()/5.0;
		double repD=range/prev.size()/10.0;

		List<Integer> sort=MatrixUtil.getOrderedValueIndexesAscend(prev);
		DoubleMatrix1D rep=prev.like();
		for(int i=0;i<sort.size();i++){
			int x=sort.get(i);
			double pos=prev.getQuick(x);
			double force=curr.getQuick(x)-prev.getQuick(x);
			double dLeft=i==0?Double.POSITIVE_INFINITY:Math.abs(prev.getQuick(sort.get(i-1))-pos);
			double dRight=i==sort.size()-1?Double.POSITIVE_INFINITY:Math.abs(prev.getQuick(sort.get(i+1))-pos);
			if(Math.abs(force)<minF){
				if(dLeft<repD&&dRight<repD){
					rep.setQuick(x,pos+(dRight-dLeft)/2);
				}
				else{
					if(force<0){
						if(i==0){
							rep.setQuick(x,curr.get(x));
						}
						else if(dLeft+force<repD){
							rep.setQuick(x,pos-(dLeft+repD));
						}
						else{
							rep.setQuick(x,curr.get(x));
						}
					}
					else if(force>0){
						if(i==sort.size()-1){
							rep.setQuick(x,curr.get(x));
						}
						else if(dRight-force<repD){
							rep.setQuick(x,pos+(dRight-repD));
						}
						else{
							rep.setQuick(x,curr.get(x));
						}
					}
					else{
						rep.setQuick(x,pos);
					}
				}
			}
			else{
				rep.setQuick(x,curr.getQuick(x));
			}
		}

		return rep;
	}

}
