package edu.cmu.cs.frank.ml.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.jet.math.Functions;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.Pair;
import edu.cmu.cs.frank.util.Parameters;
import edu.cmu.cs.frank.util.RandomUtil;
import edu.cmu.cs.frank.util.ScoreTable;

public class KMeans implements Clusterer{

	protected static Logger log=Logger.getLogger(KMeans.class);

	public static enum Seeder{
		KMEANS_PLUS_PLUS,
		RANDOM
	}

	public static enum WinningTrial{
		MIN_WCSS,
		MODE_WCSS,
		MAX_FREQ
	}

	public static class Params extends Parameters{

		static final long serialVersionUID=20080512L;

		public int k=2;
		public int trials=10;
		public Seeder seeder=Seeder.KMEANS_PLUS_PLUS;
		public WinningTrial winningTrial=WinningTrial.MIN_WCSS;
		public double convergence=0.00001;
		public boolean dense=true;
		public boolean exact2Means1D=false;

	}

	private Params p=new Params();

	@Override
	public void setParams(Parameters params){
		this.p=(Params)params;
	}

	@Override
	public Params getParams(){
		return p;
	}

	@Override
	public List<Label> cluster(Dataset dataset){
		
		Pair<DoubleMatrix2D,DoubleMatrix2D> result=cluster(Convert.toMatrix(dataset,Convert.Orientation.ROW_INSTANCE,p.dense));
		
		return ClusterUtil.toLabels(result.getB());
		
	}
	
	// core wrapped with multiple trials
	public Pair<DoubleMatrix2D,DoubleMatrix2D> cluster(DoubleMatrix2D data){

		if(p.exact2Means1D&&p.k==2&&data.columns()==1){
			//log.info("Clustering one-dimensional data into two clusters, running exact 2-means...");
			return cluster2Means1DExact(data.viewColumn(0));
		}
		
		//log.info("Running "+p.trials+" trials...");

		DoubleMatrix2D[] trialCenters=new DoubleMatrix2D[p.trials];
		DoubleMatrix2D[] trialClusterings=new DoubleMatrix2D[p.trials];
		double[] wcss=null;
		ScoreTable<String> freq=null;
		Map<String,Integer> keyIndex=null; 


		if(p.winningTrial.equals(WinningTrial.MAX_FREQ)){
			freq=new ScoreTable<String>();
			keyIndex=new HashMap<String,Integer>();
		}
		else{
			wcss=new double[p.trials];
		}

		for(int i=0;i<p.trials;i++){

			log.info("Running trial "+i+"...");

			DoubleMatrix2D initCenters;
			if(p.seeder.equals(Seeder.KMEANS_PLUS_PLUS)){
				initCenters=getKmeansPlusPlusSeeds(data,p.k);
			}
			else{
				initCenters=getRandomSeeds(data,p.k);
			}

			Pair<DoubleMatrix2D,DoubleMatrix2D> result=cluster(data,initCenters,p.convergence);
			DoubleMatrix2D centers=result.getA();
			DoubleMatrix2D clustering=result.getB();

			if(p.winningTrial.equals(WinningTrial.MAX_FREQ)){
				int mapped=0;
				int[] map=new int[clustering.columns()];
				Arrays.fill(map,-1);
				StringBuilder trialKeyBuffer=new StringBuilder();
				for(int j=0;j<clustering.rows();j++){
					int bestId=MatrixUtil.getMaxIndex(clustering.viewRow(j));
					if(map[bestId]==-1){
						map[bestId]=mapped;
						mapped++;
					}
					trialKeyBuffer.append(map[bestId]).append('|');
				}
				String trialKey=trialKeyBuffer.toString();
				freq.increment(trialKey);
				keyIndex.put(trialKey,i);
			}
			else{
				wcss[i]=getWCSS(data,centers,clustering);
				log.info("WCSS="+wcss[i]);
			}
			
			trialCenters[i]=centers;
			trialClusterings[i]=clustering;

		}

		if(p.winningTrial.equals(WinningTrial.MAX_FREQ)){
			String bestKey=freq.highestKey();
			int best=keyIndex.get(bestKey);
			log.info("Max frequency: "+(int)(double)freq.get(bestKey)+"/"+p.trials);
			return new Pair<DoubleMatrix2D,DoubleMatrix2D>(trialCenters[best],trialClusterings[best]);
		}
		else if(p.winningTrial.equals(WinningTrial.MODE_WCSS)){
			int best=ArrayUtil.modeIndex(wcss);
			log.info("Mode WCSS("+best+"): "+wcss[best]);
			return new Pair<DoubleMatrix2D,DoubleMatrix2D>(trialCenters[best],trialClusterings[best]);
		} 
		else{
			int best=ArrayUtil.minIndex(wcss);
			log.info("Min WCSS("+best+"): "+wcss[best]);
			return new Pair<DoubleMatrix2D,DoubleMatrix2D>(trialCenters[best],trialClusterings[best]);
		}

	}

	// for calculating the within-cluster sum of squares
	public static double getWCSS(DoubleMatrix2D data,DoubleMatrix2D centers,DoubleMatrix2D clustering){

		double[] wcss=new double[centers.rows()];
		for(int i=0;i<data.rows();i++){
			int assignment=MatrixUtil.getMaxIndex(clustering.viewRow(i));
			for(int j=0;j<data.columns();j++){
				double diff=data.getQuick(i,j)-centers.getQuick(assignment,j);
				wcss[assignment]+=diff*diff;
			}
		}
		return ArrayUtil.sum(wcss);

	}
	
	// the k-means core - the data should be a row-instance matrix
	// the first return value is centers - k by d matrix
	// the second return value is clustering - n by k matrix(negative distances)
	public static Pair<DoubleMatrix2D,DoubleMatrix2D> cluster(DoubleMatrix2D data,DoubleMatrix2D initCenters,double convergence){

		DoubleMatrix2D centers=initCenters;
		// make k clusters based on index and assign initial clustering
		DoubleMatrix2D clustering=assignCluster(centers,data);

		// for checking convergence
		DoubleMatrix2D prevCenters=centers;
		double delta=Double.MAX_VALUE;

		// iterate until convergence
		int t=0;
		while(delta>convergence){

			// recalculate center
			centers=findCenters(clustering,data);

			// reassign clusters
			clustering=assignCluster(centers,data);

			// caculate delta
			delta=0.0;
			for(int i=0;i<centers.rows();i++){
				delta+=MatrixUtil.getEuclideanDistance(prevCenters.viewRow(i),centers.viewRow(i));
			}
			delta=delta/centers.rows();

			// update prevCenters
			prevCenters=centers;

			t++;

		}

		//log.info("Converged in "+t+" iteration(s)");

		return new Pair<DoubleMatrix2D,DoubleMatrix2D>(centers,clustering);

	}
	
	// subroutine for the k-means core
	private static DoubleMatrix2D assignCluster(DoubleMatrix2D centers,DoubleMatrix2D data){
		DoubleMatrix2D clustering=new DenseDoubleMatrix2D(data.rows(),centers.rows());
		for(int i=0;i<clustering.rows();i++){
			for(int j=0;j<clustering.columns();j++){
				clustering.setQuick(i,j,-MatrixUtil.getEuclideanDistance(data.viewRow(i),centers.viewRow(j)));
			}
		}
		return clustering;
	}

	// subroutine for the k-means core
	private static DoubleMatrix2D findCenters(DoubleMatrix2D clustering,DoubleMatrix2D data){
		DoubleMatrix2D centers=new DenseDoubleMatrix2D(clustering.columns(),data.columns());
		int[] counts=new int[centers.rows()];
		for(int i=0;i<data.rows();i++){
			int cluster=MatrixUtil.getMaxIndex(clustering.viewRow(i));
			centers.viewRow(cluster).assign(data.viewRow(i),Functions.plus);
			counts[cluster]++;
		}
		for(int i=0;i<centers.rows();i++){
			if(counts[i]==0){
				centers.viewRow(i).assign(0.0);
			}
			else{
				centers.viewRow(i).assign(Functions.div(counts[i]));
			}
		}
		return centers;
	}

	// simple random seeding
	public static DoubleMatrix2D getRandomSeeds(DoubleMatrix2D data,int numCenters){
		List<Integer> centerRows=RandomUtil.drawInteger(data.rows(),numCenters);
		DoubleMatrix2D centers=new DenseDoubleMatrix2D(numCenters,data.columns());
		for(int i=0;i<centers.rows();i++){
			centers.viewRow(i).assign(data.viewRow(centerRows.get(i)));
		}
		return centers;
	}

	// Arthur and Vassilvitskii. Kmeans++: the advantages of careful seeding.
	public static DoubleMatrix2D getKmeansPlusPlusSeeds(DoubleMatrix2D data,int numCenters){

		Random rand=new Random();

		// initialize uniform distribution
		double[] distribution=new double[data.rows()];
		for(int i=0;i<data.rows();i++){
			distribution[i]=1.0/data.rows();
		}

		List<DoubleMatrix1D> seeds=new ArrayList<DoubleMatrix1D>(numCenters);

		// get first seed uniformly
		int nextSeed=RandomUtil.drawWithDistribution(distribution,rand);
		seeds.add(data.viewRow(nextSeed));

		// rest get based on distribution bias according to previous seeds
		for(int i=1;i<numCenters;i++){
			calculateDistribution(data,distribution,seeds);
			nextSeed=RandomUtil.drawWithDistribution(distribution,rand);
			seeds.add(data.viewRow(nextSeed));
		}

		DoubleMatrix2D seedMatrix=new SparseDoubleMatrix2D(numCenters,data.columns());
		for(int i=0;i<seeds.size();i++){
			seedMatrix.viewRow(i).assign(seeds.get(i));
		}

		return seedMatrix;

	}

	// subroutine for Kmeans++
	private static void calculateDistribution(DoubleMatrix2D data,double[] distribution,List<DoubleMatrix1D> seeds){
		double totalDist=0.0;
		for(int i=0;i<data.rows();i++){
			// find distance to the closest seed
			double minDist=Double.MAX_VALUE;
			for(int j=0;j<seeds.size();j++){
				double dist=MatrixUtil.getEuclideanDistance(data.viewRow(i),seeds.get(j));
				if(minDist>dist){
					minDist=dist;
				}
			}
			// decrease distribution for that seed
			distribution[i]=Math.pow(minDist,2);
			totalDist+=distribution[i];
		}
		// uniformize
		for(int i=0;i<distribution.length;i++){
			distribution[i]/=totalDist;
		}
	}
	
	// k-means can be calculated exactly in a reasonable time for one dimension and k=2
	public static Pair<DoubleMatrix2D,DoubleMatrix2D> cluster2Means1DExact(DoubleMatrix1D data){

		double[] values=data.toArray();
		Arrays.sort(values);
		double[] wcss=wcss2Means1DExactLinear(values);
		
		int best=ArrayUtil.minIndex(wcss);
		log.info("Min WCSS("+best+"): "+wcss[best]);
		
		double meanLeft=0.0;
		for(int i=0;i<best+1;i++){
			meanLeft+=values[i];
		}
		meanLeft/=best+1;
		
		double meanRight=0.0;
		for(int i=best+1;i<values.length;i++){
			meanRight+=values[i];
		}
		meanRight/=values.length-(best+1);

		DoubleMatrix2D centers=new DenseDoubleMatrix2D(2,1);
		centers.setQuick(0,0,meanLeft);
		centers.setQuick(1,0,meanRight);
		
		DoubleMatrix2D clustering=new DenseDoubleMatrix2D(data.size(),2);
		for(int i=0;i<data.size();i++){
			clustering.setQuick(i,0,-Math.abs(data.getQuick(i)-meanLeft));
			clustering.setQuick(i,1,-Math.abs(data.getQuick(i)-meanRight));
		}

		return new Pair<DoubleMatrix2D,DoubleMatrix2D>(centers,clustering);

	}

	// assumes values are sorted
	public static double[] wcss2Means1DExactQuadratic(double[] values){

		double[] sumLeft=new double[values.length-1];
		sumLeft[0]=values[0];
		for(int i=1;i<sumLeft.length;i++){
			sumLeft[i]=sumLeft[i-1]+values[i];
		}

		double[] sumRight=new double[values.length-1];
		sumRight[0]=values[values.length-1];
		for(int i=1;i<sumRight.length;i++){
			sumRight[i]=sumRight[i-1]+values[values.length-i-1];
		}
		ArrayUtil.reverse(sumRight);

		double[] wcss=new double[values.length-1];
		for(int i=0;i<wcss.length;i++){
			int nLeft=i+1;
			double meanLeft=sumLeft[i]/(i+1);
			for(int j=0;j<i+1;j++){
				double diff=values[j]-meanLeft;
				wcss[i]+=diff*diff;
			}
			int nRight=values.length-nLeft;
			double meanRight=sumRight[i]/nRight;
			for(int j=i+1;j<values.length;j++){
				double diff=values[j]-meanRight;
				wcss[i]+=diff*diff;
			}
		}
		
		return wcss;
		
	}
	
	// assumes values are sorted
	public static double[] wcss2Means1DExactLinear(double[] values){

		double[] lrPass=wcssLinearPass(values);
		ArrayUtil.reverse(values);
		double[] rlPass=wcssLinearPass(values);
	
		double[] wcss=new double[values.length-1];
		for(int i=0;i<wcss.length;i++){
			wcss[i]=lrPass[i]+rlPass[wcss.length-i-1];
		}
		
		return wcss;
		
	}
	
	// assumes values are sorted
	private static double[] wcssLinearPass(double[] values){
		
		double[] wcss=new double[values.length-1];
		wcss[0]=0.0;
		double sum=values[0];
		for(int i=1;i<wcss.length;i++){
			double oldMean=sum/i;
			double newMean=(sum+values[i])/(i+1);
			double diff=values[i]-newMean;
			wcss[i]=wcss[i-1]+diff*diff+(newMean*newMean-oldMean*oldMean)*i+2*(oldMean-newMean)*sum;
			sum+=values[i];
		}
	
		return wcss;
		
	}
	
	public static void main(String[] args){
		
		double[] values={2,4,6,10,12,14};
		
		System.out.println("Values:");
		System.out.println(Arrays.toString(values));
		
		double[] q=wcss2Means1DExactQuadratic(values);
		System.out.println("Quadratic:");
		System.out.println(Arrays.toString(q));
		System.out.println("Min: "+ArrayUtil.min(q));
		System.out.println("MinIndex: "+ArrayUtil.minIndex(q));
		
		double[] l=wcss2Means1DExactLinear(values);
		System.out.println("Linear:");
		System.out.println(Arrays.toString(l));
		System.out.println("Min: "+ArrayUtil.min(l));
		System.out.println("MinIndex: "+ArrayUtil.minIndex(l));
		
	}

}