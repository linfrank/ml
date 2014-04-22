package edu.cmu.cs.frank.ml.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.stat.test.NormalDist;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.Pair;
import edu.cmu.cs.frank.util.Parameters;

/**
 * A modified version of G-means in "Learning the k in k-means"
 */

public class GMeans implements Clusterer{

	protected static Logger log=Logger.getLogger(GMeans.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20080512L;

		public double alpha=0.005;
		public int minClusterSize=1;
		public double minStdDev=1.0e-16;
		public KMeans.Params kMeansParams=new KMeans.Params();
		public boolean dense=true;

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

	@Override
	public List<Label> cluster(Dataset dataset){

		DoubleMatrix2D data=Convert.toMatrix(dataset,Convert.Orientation.ROW_INSTANCE,p.dense);

		return ClusterUtil.toLabels(cluster(data).getB());

	}

	public Pair<DoubleMatrix2D,DoubleMatrix2D> cluster(DoubleMatrix2D data){

		// initialize k-means with a single center
		DoubleMatrix2D centers=MatrixUtil.getMean(data,MatrixUtil.Orientation.ROW);
		DoubleMatrix2D clustering=new DenseDoubleMatrix2D(0,0);

		boolean stop;

		do{

			stop=true;

			log.info("Trying "+centers.rows()+" center(s)...");
			// run k-means to determine a clustering for current k centers
			Pair<DoubleMatrix2D,DoubleMatrix2D> result=KMeans.cluster(data,centers,p.kMeansParams.convergence);

			// put result in clusters
			Map<Integer,List<DoubleMatrix1D>> clusters=new TreeMap<Integer,List<DoubleMatrix1D>>();
			for(int i=0;i<result.getB().rows();i++){
				int c=MatrixUtil.getMaxIndex(result.getB().viewRow(i));
				if(!clusters.containsKey(c)){
					clusters.put(c,new ArrayList<DoubleMatrix1D>());
				}
				clusters.get(c).add(data.viewRow(i));
			}

			// possible new centers
			List<DoubleMatrix1D> newCenters=new ArrayList<DoubleMatrix1D>();

			// go through each cluster
			for(int c:clusters.keySet()){

				log.info("Testing cluster "+c+"...");
				List<DoubleMatrix1D> members=clusters.get(c);
				log.info("Cluster "+c+" members: "+members.size());

				// case zero member cluster
				if(members.size()<1){
					log.info("Cluster "+c+" has no member, removing current center.");
				}
				// case min # members cluster
				else if(members.size()<=p.minClusterSize){
					log.info("Cluster "+c+" has only "+members.size()+" member(s), keeping current center.");
					newCenters.add(result.getA().viewRow(c));	
				}
				// case we need to test cluster-ness statistically
				else{

					// array for 1-d projection
					double[] projection=new double[members.size()];

					// we may need this when splitting
					DoubleMatrix2D subcenters=null;

					// avoid projection for 1-d case
					if(centers.columns()==1){
						log.info("Testing distribution in 1-d, copying data points directly...");
						for(int i=0;i<projection.length;i++){
							projection[i]=members.get(i).getQuick(0);
						}
					}
					// project data to 1-d
					else{
						log.info("Testing distribution in "+data.columns()+"-d, projecting...");
						// create a sub-dataset within this cluster
						DoubleMatrix2D subdata=new DenseDoubleMatrix2D(members.size(),data.columns());
						for(int i=0;i<members.size();i++){
							subdata.viewRow(i).assign(members.get(i));
						}
						// create two centers with k-means
						KMeans kMeans=new KMeans();
						kMeans.setParams(p.kMeansParams);
						subcenters=kMeans.cluster(subdata).getA();
						// create a vector between these two centers
						DoubleMatrix1D v=subcenters.like1D(subcenters.columns());
						double vnorm=0.0;
						for(int i=0;i<subcenters.columns();i++){
							double diff=subcenters.getQuick(0,i)-subcenters.getQuick(1,i);
							v.setQuick(i,diff);
							vnorm+=diff*diff;
						}
						// project data points onto v
						for(int i=0;i<projection.length;i++){
							projection[i]=MatrixUtil.getDotProduct(subdata.viewRow(i),v)/vnorm;
						}
					}

					// transform to mean 0 variance 1
					double mean=ArrayUtil.mean(projection);
					double stdDev=ArrayUtil.stdDev(projection,mean);
					log.info("Mean: "+mean+" StdDev: "+stdDev);
					
//					if(Double.isNaN(mean)||Double.isNaN(stdDev)){
//						log.error("NaN!!");
//						log.error("projection: "+Arrays.toString(projection));
//						log.error("original data:");
//						for(int i=0;i<data.rows();i++){
//							log.error(" "+i+"\t"+data.viewRow(i));
//						}
//						System.exit(1);
//					}
					
					ArrayUtil.normalizeStat(projection,mean,stdDev);
					// sort
					Arrays.sort(projection);

					// test
					if(stdDev<p.minStdDev){
						log.info("Cluster "+c+" is (near) constant, keeping current center.");
						newCenters.add(result.getA().viewRow(c));	
					}
					else if(andersonDarlingNormality(projection,p.alpha)){
						log.info("Cluster "+c+" is normal, keeping current center.");
						newCenters.add(result.getA().viewRow(c));	
					}
					else{
						log.info("Cluster "+c+" is not normal, splitting...");

						// for 1-d case since we haven't created centers yet
						if(centers.columns()==1){
							// create a sub-dataset within this cluster
							DoubleMatrix2D subdata=new DenseDoubleMatrix2D(members.size(),1);
							for(int i=0;i<members.size();i++){
								subdata.setQuick(i,0,members.get(i).getQuick(0));
							}
							// create two centers with k-means, do exact 2-means in 1-d
							KMeans kMeans=new KMeans();
							kMeans.setParams(new KMeans.Params());
							kMeans.getParams().trials=1;
							kMeans.getParams().exact2Means1D=true;
							subcenters=kMeans.cluster(subdata).getA();
						}

						// put in list of new centers
						for(int i=0;i<subcenters.rows();i++){
							newCenters.add(subcenters.viewRow(i));
						}
						stop=false;
					}
				}
			}

			if(!stop&&centers.rows()>data.rows()){
				log.warn("Too many centers, stopping prematurely.");
				stop=true;
			}

			if(stop){
				centers=result.getA();
				clustering=result.getB();
			}
			else{
				centers=new DenseDoubleMatrix2D(newCenters.size(),data.columns());
				for(int i=0;i<newCenters.size();i++){
					centers.viewRow(i).assign(newCenters.get(i));
				}
			}

		}while(!stop);

		return new Pair<DoubleMatrix2D,DoubleMatrix2D>(centers,clustering);

	}

	// Anderson-Darling test for normal or lognormal distribution, assuming mean 0 and variance 1
	public static double alphas[]={0.500,0.250,0.150,0.100,0.050,0.025,0.010,0.005,0.0001};
	public static double values[]={0.341,0.470,0.561,0.631,0.752,0.873,1.035,1.159,1.8692};
	// returns true if there's not a good chance that the data did not rise from normality
	private static boolean andersonDarlingNormality(double[] data,double alpha){
		double a=aSquareEst(data);
		log.info("Corrected A^2 statistic: "+a);
		for(int i=0;i<alphas.length;i++){
			if(alpha==alphas[i]){
				return a<=values[i];
			}
		}
		log.warn("Anderson-Darling test alpha not found in table: "+alpha);
		return false;
	}

	// the correct A^2 statistic when estimating from data
	private static double aSquareEst(double[] data){
		int n=data.length;
		return aSquare(data)*(1.0+4.0/n-25.0/(n*n));
	}

	// A^2 statistic for Anderson-Darling normality test
	private static double aSquare(double[] data){
		int n=data.length;
		double[] z=new double[n];
		for(int i=0;i<n;i++){
			z[i]=standardNormalCDF(data[i]);
		}
		double sum=0.0;
		for(int i=0;i<n;i++){
			sum+=(2*(i+1)-1.0)/n*(Math.log(z[i])+Math.log(1.0-z[n-1-i]));
		}

		return -n-sum;
	}

	// CDF function for N(0,1)
	private static double sqrt2=Math.sqrt(2.0);
	private static double standardNormalCDF(double x){
		return 0.5*(1.0+NormalDist.erf(x/sqrt2));
	}




}