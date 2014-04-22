package edu.cmu.cs.frank.x;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.cs.frank.ml.cluster.KMeans;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.stat.test.NormalDist;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.Parameters;
import edu.cmu.cs.frank.util.ScoreTable;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;


public class PICDisambiguator implements Curator{

	static Logger log=Logger.getLogger(PICDisambiguator.class);

	public static class Params extends Parameters{
		
		static final long serialVersionUID=20111103L;
		
		public int dimensions=2;
		
	}
	
	@Override
	public void setParams(Parameters p){
		this.p=(Params)p;
	}

	@Override
	public Parameters getParams(){
		return p;
	}

	private Params p=new Params();
	
	public PICDisambiguator(int dimensions){
		p.dimensions=dimensions;
	}

	@Override
	public Map<Integer,String> curate(DirectedGraph<String,Integer> graph){
		
		Map<Integer,String> pred=new HashMap<Integer,String>();
		
		return pred;
	}
	
	@Override
	public ScoreTable<String> getAmbiguityScore(DirectedGraph<String,Integer> graph){
		
		log.info("Running PIC");
		
		// holds PIC values
		Map<String,double[]> vals=new HashMap<String,double[]>(graph.getVertexCount());
		
		// initialize values
		Random rand=new Random();
		for(String name:graph.getVertices()){
			double[] val=new double[p.dimensions];
			for(int i=0;i<val.length;i++){
				val[i]=rand.nextDouble();
			}
			vals.put(name,val);
		}
		
		// run some iterations
		vals=iterate(graph,vals,30);
		
		log.info("Testing normality in clusters");
		
		// test normality
		ScoreTable<String> nscore=new ScoreTable<String>();
		
		for(String name:graph.getVertices()){
			// only do authors with more than one paper
			if(name.startsWith("N:")&&graph.degree(name)>1){
				// create subdata
				DoubleMatrix2D subdata=new DenseDoubleMatrix2D(graph.getNeighborCount(name),p.dimensions);
				Iterator<String> neighbors=graph.getNeighbors(name).iterator();
				for(int i=0;neighbors.hasNext();i++){
					subdata.viewRow(i).assign(vals.get(neighbors.next()));
				}
				// create two centers with k-means
				DoubleMatrix2D subcenters=KMeans.cluster(subdata,KMeans.getRandomSeeds(subdata,2),0.00001).getA();
				// create a vector between these two centers
				DoubleMatrix1D v=subcenters.like1D(subcenters.columns());
				double vnorm=0.0;
				for(int i=0;i<subcenters.columns();i++){
					double diff=subcenters.getQuick(0,i)-subcenters.getQuick(1,i);
					v.setQuick(i,diff);
					vnorm+=diff*diff;
				}
				// project data points onto v
				double[] stat=new double[graph.getNeighborCount(name)];
				for(int i=0;i<stat.length;i++){
					stat[i]=MatrixUtil.getDotProduct(subdata.viewRow(i),v)/vnorm;
				}
				ArrayUtil.normalizeStat(stat);
				Arrays.sort(stat);
				nscore.set(name,aSquareEst(stat));
			}
		}
		
		return nscore;
		
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
	
	public Map<String,double[]> iterate(Graph<String,Integer> graph,Map<String,double[]> vals,int maxIt){
		Map<String,double[]> oldVals=vals;
		Map<String,double[]> newVals=new HashMap<String,double[]>(oldVals.size());
		for(int i=0;i<maxIt;i++){
			for(String v:graph.getVertices()){
				double[] newVal=newVals.get(v);
				if(newVal==null){
					newVal=new double[p.dimensions];
					newVals.put(v,newVal);
				}
				for(String n:graph.getNeighbors(v)){
					for(int j=0;j<p.dimensions;j++){
						newVal[j]+=oldVals.get(n)[j];
					}
				}
				for(int j=0;j<p.dimensions;j++){
					newVal[j]=newVal[j]/graph.getNeighborCount(v);
				}
			}
			// swap
			Map<String,double[]> temp=oldVals;
			oldVals=newVals;
			newVals=temp;
		}
		return oldVals;
	}

}
