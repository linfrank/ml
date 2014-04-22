package edu.cmu.cs.frank.ml.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.LabelSet;
import edu.cmu.cs.frank.ml.cluster.PIC.ConvergeCheck;
import edu.cmu.cs.frank.ml.cluster.PIC.InitialVector;
import edu.cmu.cs.frank.ml.cluster.PIC.VectorNormalizer;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.ml.util.MatrixUtil.Orientation;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.Pair;
import edu.cmu.cs.frank.util.Parameters;

/**
 * @author Frank Lin
 * 
 * Power Iteration Clustering with G-means
 * 
 * Hierarchical clustering alternating between PIC and G-means
 * 
 */

public class PICH implements ProjectionClusterer{

	protected static Logger log=Logger.getLogger(PICH.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20080512L;

		public InitialVector initialVector=InitialVector.RandomReal;
		public ConvergeCheck convergeCheck=ConvergeCheck.Accel;
		public VectorNormalizer vectorNormalizer=VectorNormalizer.Sum;
		public double convergence=0.0001;
		public int maxT=1000;
		public int numRuns=1;
		public boolean fixedRunSeed=false;
		public int runSeed=0;
		public GMeans.Params gMeansParams=new GMeans.Params();
		public double laplacianAlpha=1.0;
		public int checkFrequency=1;
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

		long time=System.currentTimeMillis();

		log.info("Deriving symmetric affinity matrix...");
		DoubleMatrix2D a=Convert.toMatrix(dataset,Convert.Orientation.ROW_INSTANCE,p.dense);
		MatrixUtil.makeSymmetricMax(a);

		// list for storing cluster names
		List<String> nameList=new ArrayList<String>();
		// list for storing cluster members
		List<int[]> membersList=new ArrayList<int[]>();
		// list for storing leaf clusters
		List<Integer> leaves=new ArrayList<Integer>();

		// insert the original matrix into the lists
		nameList.add("0");
		int[] all=new int[a.rows()];
		for(int i=0;i<all.length;i++){
			all[i]=i;
		}
		membersList.add(all);

		for(int i=0;i<nameList.size();i++){
			// retrieve current name and submatrix
			String name=nameList.get(i);
			int[] members=membersList.get(i);
			log.info("Processing cluster "+name+" with "+members.length+" member(s)...");
			// construct the submatrix
			DoubleMatrix2D aSub=a.viewSelection(members,members);

			// check to see if the matrix has zero items
			boolean fitToPIC=true;
			for(int j=0;j<aSub.rows()&&fitToPIC;j++){
				if(aSub.viewRow(j).zSum()==0.0){
					log.info("Not fit for PIC.");
					fitToPIC=false;
				}
			}

			if(fitToPIC){
				log.info("Running PIC projection...");
				DoubleMatrix2D pSub=project(aSub);

//				if(Double.isNaN(pSub.getQuick(0,0))){
//					log.error("NaN!!");
//					log.error(Arrays.toString(members));
//					log.error(aSub);
//					System.exit(1);
//				}

				log.info("Running G-means...");
				GMeans gMeans=new GMeans();
				gMeans.setParams(p.gMeansParams);
				Pair<DoubleMatrix2D,DoubleMatrix2D> result=gMeans.cluster(pSub);
				DoubleMatrix2D centers=result.getA();
				DoubleMatrix2D clustering=result.getB();

				if(centers.rows()==1){
					log.info("Cluster "+name+" is a leaf cluster.");
					leaves.add(i);
				}
				else{
					log.info("Cluster "+name+" has "+centers.rows()+" child clusters.");
					for(int j=0;j<centers.rows();j++){
						nameList.add(nameList.get(i)+"."+j);
						List<Integer> sub=new ArrayList<Integer>();
						for(int k=0;k<clustering.rows();k++){
							if(MatrixUtil.getMaxIndex(clustering.viewRow(k))==j){
								sub.add(members[k]);
							}
						}
						membersList.add(ArrayUtil.toIntArray(sub));
					}
				}
			}
			else{
				log.info("Cluster "+name+" is a leaf cluster.");
				leaves.add(i);
			}
		}

		time=System.currentTimeMillis()-time;
		log.info("Core running time: "+time+" ms.");

		log.info("Constructing labels from leaf clusters...");
		LabelSet labelSet=new LabelSet();
		Label[] labels=new Label[dataset.size()];
		for(int i=0;i<leaves.size();i++){
			String name=nameList.get(leaves.get(i));
			int[] members=membersList.get(leaves.get(i));
			for(int j=0;j<members.length;j++){
				if(labels[members[j]]!=null){
					log.warn("Instance "+members[j]+" has multiple labels!");
				}
				else{
					labels[members[j]]=labelSet.newLabel(name);
				}
			}
		}


		return ArrayUtil.toList(labels);

	}

	/**
	 * 
	 * @param a - the affinity matrix, row-instance
	 * @return the low-dimensional projection, row-instance
	 */

	public DoubleMatrix2D project(DoubleMatrix2D a){

		log.info("Creating initial vectors...");
		if(p.initialVector.equals(InitialVector.RandomReal)){
			if(p.fixedRunSeed){
				projection=MatrixUtil.getRandomReal(a.rows(),p.numRuns,Orientation.COLUMN,new Random(p.runSeed));
			}
			else{
				projection=MatrixUtil.getRandomReal(a.rows(),p.numRuns,Orientation.COLUMN);
			}
		}
		else if(p.initialVector.equals(InitialVector.Degree)){
			p.numRuns=1;
			projection=new DenseDoubleMatrix2D(a.rows(),1);
			projection.viewColumn(0).assign(MatrixUtil.getDegreeVector(a));
			MatrixUtil.normalize(projection.viewColumn(0));
		}

		log.info("Transforming affinity matrix to transition matrix...");
		DoubleMatrix2D w=a.copy();
		MatrixUtil.normalizeRows(w);

		iterate(w,projection);

		return projection;

	}

	public void iterate(DoubleMatrix2D matrix,DoubleMatrix2D vectors){

		// set up convergence check vars
		boolean checkSpeed=p.convergeCheck.equals(ConvergeCheck.Speed);
		boolean checkAccel=p.convergeCheck.equals(ConvergeCheck.Accel);
		boolean[] converged=new boolean[p.numRuns];
		DoubleMatrix2D prevVecs=checkSpeed||checkAccel?vectors.like():null;
		DoubleMatrix2D prevDelta=checkAccel?vectors.like():null;

		log.info("Running power iteration over "+vectors.columns()+" random vectors...");

		double threshold=p.convergence*p.checkFrequency/matrix.rows();
		log.info("Adjusted convergence threshold: "+threshold);

		int t=0;
		while(t<p.maxT&&!ArrayUtil.and(converged)){

			// linear transform each vector
			for(int j=0;j<vectors.columns();j++){
				if(!converged[j]){

					DoubleMatrix1D vector=vectors.viewColumn(j);

					// matrix-vector multiplication
					vector.assign(alg.mult(matrix,vector));

					// normalize
					if(p.vectorNormalizer.equals(VectorNormalizer.Max)){
						MatrixUtil.normalizeBy(vector,MatrixUtil.getMax(vector));
					}
					else{
						MatrixUtil.normalize(vector);
					}
				}
			}

			// check convergence for each vector
			if(t%p.checkFrequency==0){
				for(int j=0;j<vectors.columns();j++){
					boolean under=true;
					if(checkSpeed){
						for(int i=0;under&&i<vectors.rows();i++){
							double diff=vectors.getQuick(i,j)-prevVecs.getQuick(i,j);
							under=Math.abs(diff)<threshold;
						}
						prevVecs.viewColumn(j).assign(vectors.viewColumn(j));
						if(under){
							converged[j]=true;
							log.info("Vector "+j+" converged at t="+t);
						}
					}
					else if(checkAccel){
						for(int i=0;under&&i<vectors.rows();i++){
							double diff=vectors.getQuick(i,j)-prevVecs.getQuick(i,j);
							under=Math.abs(diff-prevDelta.getQuick(i,j))<threshold;
							prevDelta.setQuick(i,j,diff);
						}
						prevVecs.viewColumn(j).assign(vectors.viewColumn(j));
						if(under){
							converged[j]=true;
							log.info("Vector "+j+" converged at t="+t);
						}
					}
				}
			}
			t++;
		}

		log.info("Finished in "+t+" iterations.");

	}

}
