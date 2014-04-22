package edu.cmu.cs.frank.ml.util;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import cern.jet.math.Mult;
import cern.jet.math.PlusMult;
import edu.cmu.cs.frank.util.Pair;
import edu.cmu.cs.frank.util.ScoreTable;

/**
 * @author Frank Lin
 */

public class MatrixUtil{

	public static enum Orientation{ROW,COLUMN};

	private static Algebra alg=new Algebra();

	private static Random rand=new Random();
	
	public static void makeSymmetricMaxSparse(DoubleMatrix2D a){
		a.assign(a.viewDice(),MatrixUtil.MAX);
	}
	
	public static void makeSymmetricMaxDense(DoubleMatrix2D a){
		for(int i=0;i<a.rows();i++){
			for(int j=0;j<a.columns()-i;j++){
				double max=Math.max(a.getQuick(i,j),a.getQuick(j,i));
				a.setQuick(i,j,max);
				a.setQuick(j,i,max);
			}
		}
	}
	
	public static void makeSymmetricMax(DoubleMatrix2D a){
		if(a instanceof SparseDoubleMatrix2D){
			makeSymmetricMaxSparse(a);
		}
		else{
			makeSymmetricMaxDense(a);
		}
	}
	
	public static void makeSymmetricSumSparse(DoubleMatrix2D a){
		a.assign(a.viewDice(),MatrixUtil.ADD);
	}
	
	public static void makeSymmetricSumDense(DoubleMatrix2D a){
		for(int i=0;i<a.rows();i++){
			for(int j=0;j<a.columns()-i;j++){
				double sum=a.getQuick(i,j)+a.getQuick(j,i);
				a.setQuick(i,j,sum);
				a.setQuick(j,i,sum);
			}
		}
	}
	
	public static void makeSymmetricSum(DoubleMatrix2D a){
		if(a instanceof SparseDoubleMatrix2D){
			makeSymmetricSumSparse(a);
		}
		else{
			makeSymmetricSumDense(a);
		}
	}
	
	public static double getMean(DoubleMatrix1D vector){
		return vector.zSum()/vector.size();
	}
	
	public static DoubleMatrix1D getDiff(DoubleMatrix1D a,DoubleMatrix1D b){
		DoubleMatrix1D diff=a.like();
		for(int i=0;i<diff.size();i++){
			diff.setQuick(i,a.getQuick(i)-b.getQuick(i));
		}
		return diff;
	}
	
	public static DoubleMatrix1D getAbsDiff(DoubleMatrix1D a,DoubleMatrix1D b){
		DoubleMatrix1D diff=a.like();
		for(int i=0;i<diff.size();i++){
			diff.setQuick(i,Math.abs(a.getQuick(i)-b.getQuick(i)));
		}
		return diff;
	}

	public static DoubleMatrix1D getMean(Collection<DoubleMatrix1D> vectors){
		DoubleMatrix1D mean=null;
		for(DoubleMatrix1D vector:vectors){
			if(mean==null){
				mean=new DenseDoubleMatrix1D(vector.size());
			}
			mean.assign(vector,Functions.plus);
		}
		mean.assign(Functions.div(vectors.size()));
		return mean;
	}
	
	public static DoubleMatrix2D getMean(DoubleMatrix2D matrix,Orientation orientation){
		DoubleMatrix2D mean;
		if(orientation.equals(Orientation.ROW)){
			mean=new DenseDoubleMatrix2D(1,matrix.columns());
			for(int i=0;i<matrix.columns();i++){
				mean.setQuick(0,i,matrix.viewColumn(i).zSum()/matrix.rows());
			}
		}
		else{
			mean=new DenseDoubleMatrix2D(matrix.rows(),1);
			for(int i=0;i<matrix.rows();i++){
				mean.setQuick(i,0,matrix.viewRow(i).zSum()/matrix.columns());
			}
		}
		return mean;
	}
	
	public static double getStdDev(DoubleMatrix1D vector){
		double mean=getMean(vector);
		double sum=0.0;
		for(int i=0;i<vector.size();i++){
			double diff=vector.getQuick(i)-mean;
			sum+=diff*diff;
		}
		return Math.sqrt(sum/vector.size());
	}

	public static double getEuclideanDistance(DoubleMatrix1D a,DoubleMatrix1D b){
		assert a.size()==b.size();
		double sum=0.0;
		for(int i=0;i<a.size();i++){
			sum+=Math.pow(a.getQuick(i)-b.getQuick(i),2);
		}
		return Math.sqrt(sum);
	}
	
	public static double getDotProduct(DoubleMatrix1D a,DoubleMatrix1D b){
		assert a.size()==b.size();
		int size=a.size();
		double sum=0.0;
		for(int i=0;i<size;i++){
			sum+=a.getQuick(i)*b.getQuick(i);
		}
		return sum;
	}

	public static double getCosineSimilarity(DoubleMatrix1D a,DoubleMatrix1D b){
		assert a.size()==b.size();
		double sim=a.zDotProduct(b)/(alg.norm2(a)*alg.norm2(b));
		return sim;
	}

	public static DoubleMatrix2D getIdentityMatrix(int size){
		DoubleMatrix2D a=new SparseDoubleMatrix2D(size,size);
		for(int i=0;i<size;i++){
			a.setQuick(i,i,1.0);
		}
		return a;
	}

	public static DoubleMatrix2D getDegreeMatrix(DoubleMatrix2D w){
		DoubleMatrix2D d=new SparseDoubleMatrix2D(w.rows(),w.columns());
		for(int i=0;i<w.rows();i++){
			double sum=0.0;
			for(int j=0;j<w.columns();j++){
				sum+=w.getQuick(i,j);
			}
			d.setQuick(i,i,sum);
		}
		return d;
	}
	
	public static DoubleMatrix1D getDegreeVector(DoubleMatrix2D w){
		DoubleMatrix1D d=new DenseDoubleMatrix1D(w.rows());
		for(int i=0;i<w.rows();i++){
			double sum=0.0;
			for(int j=0;j<w.columns();j++){
				sum+=w.getQuick(i,j);
			}
			d.setQuick(i,sum);
		}
		return d;
	}

	public static DoubleMatrix1D getRowSumVector(DoubleMatrix2D a){
		DoubleMatrix1D d=new DenseDoubleMatrix1D(a.rows());
		for(int i=0;i<a.rows();i++){
			double sum=0.0;
			for(int j=0;j<a.columns();j++){
				sum+=a.getQuick(i,j);
			}
			d.setQuick(i,sum);
		}
		return d;
	}

	public static DoubleMatrix1D getColumnSumVector(DoubleMatrix2D a){
		DoubleMatrix1D d=new DenseDoubleMatrix1D(a.columns());
		for(int i=0;i<a.columns();i++){
			double sum=0.0;
			for(int j=0;j<a.rows();j++){
				sum+=a.getQuick(j,i);
			}
			d.setQuick(i,sum);
		}
		return d;
	}

	// As in Luxburg tutorial
	public static DoubleMatrix2D getUnnormalizedLaplacianMatrix(DoubleMatrix2D w){
		return getDegreeMatrix(w).assign(w,SUBTRACT);
	}

	// As L_rw in Luxburg tutorial
	public static DoubleMatrix2D getNormalizedLaplacianMatrix1(DoubleMatrix2D w){
		DoubleMatrix2D dInv=getDegreeMatrix(w);
		for(int i=0;i<dInv.rows();i++){
			dInv.setQuick(i,i,1.0/dInv.getQuick(i,i));
		}
		return alg.mult(dInv,getUnnormalizedLaplacianMatrix(w));
	}

	// As L_sym in Luxburg tutorial
	public static DoubleMatrix2D getNormalizedLaplacianMatrix2(DoubleMatrix2D w){
		DoubleMatrix2D dInv=getDegreeMatrix(w);
		for(int i=0;i<dInv.rows();i++){
			dInv.setQuick(i,i,Math.sqrt(1.0/dInv.getQuick(i,i)));
		}
		DoubleMatrix2D l=getUnnormalizedLaplacianMatrix(w);
		return alg.mult(alg.mult(dInv,l),dInv);
	}

	// Frank's version
	public static DoubleMatrix2D getNormalizedLaplacianMatrix1(DoubleMatrix2D w,double alpha){
		DoubleMatrix2D dInv=getDegreeMatrix(w);
		for(int i=0;i<dInv.rows();i++){
			dInv.setQuick(i,i,1.0/Math.pow(dInv.getQuick(i,i),alpha));
		}
		return alg.mult(dInv,getUnnormalizedLaplacianMatrix(w));
	}

	// Frank's version
	public static DoubleMatrix2D getNormalizedLaplacianMatrix2(DoubleMatrix2D w,double alpha){
		DoubleMatrix2D dInv=getDegreeMatrix(w);
		for(int i=0;i<dInv.rows();i++){
			dInv.setQuick(i,i,Math.pow(1.0/dInv.getQuick(i,i),0.5*alpha));
		}
		DoubleMatrix2D l=getUnnormalizedLaplacianMatrix(w);
		return alg.mult(alg.mult(dInv,l),dInv);
	}

	public static DoubleMatrix1D getRandomReal(int size,Random random){
		DoubleMatrix1D v=new DenseDoubleMatrix1D(size);
		for(int i=0;i<size;i++){
			v.setQuick(i,random.nextDouble());
		}
		return v;
	}

	public static DoubleMatrix1D getRandomReal(int size){
		return getRandomReal(size,rand);
	}
	
	public static DoubleMatrix1D getRandomInt(int size,int min,int max,Random random){
		DoubleMatrix1D v=new DenseDoubleMatrix1D(size);
		int range=max-min;
		for(int i=0;i<size;i++){
			v.setQuick(i,random.nextInt(range)+min);
		}
		return v;
	}
	
	public static DoubleMatrix1D getRandomInt(int size,int min,int max){
		return getRandomInt(size,min,max,rand);
	}

	public static DoubleMatrix2D getRandomReal(int rows,int columns,Orientation fillFirst,Random random){
		DoubleMatrix2D a=new DenseDoubleMatrix2D(rows,columns);
		if(fillFirst.equals(Orientation.ROW)){
			for(int i=0;i<rows;i++){
				for(int j=0;j<columns;j++){
					a.setQuick(i,j,random.nextDouble());
				}
			}
		}
		else{
			for(int j=0;j<columns;j++){
				for(int i=0;i<rows;i++){
					a.setQuick(i,j,random.nextDouble());
				}
			}
		}
		return a;
	}

	public static DoubleMatrix2D getRandomReal(int rows,int columns,Orientation fillFirst){
		return getRandomReal(rows,columns,fillFirst,rand);
	}

	public static Pair<Double,DoubleMatrix1D> powerMethod(DoubleMatrix2D a,double convergence){

		// random initial vector
		DoubleMatrix1D v=getRandomReal(a.rows());

		// max, converges to leading eigenvalue
		double m=Double.MIN_VALUE;

		double delta=Double.MAX_VALUE;

		DoubleMatrix1D prev;
		while(delta<convergence){
			prev=v;
			v=alg.mult(a,v);
			m=MatrixUtil.getMax(v);
			normalizeBy(v,m);
			delta=getEuclideanDistance(prev,v);
		}

		return new Pair<Double,DoubleMatrix1D>(m,v);

	}

	public static DoubleMatrix1D getPageRankVector(
			final DoubleMatrix2D transition,
			final DoubleMatrix1D person,
			double teleProb,
			int maxT,
			double convergence
	){

		// create and initialize the PageRank rank vector
		DoubleMatrix1D pr=new DenseDoubleMatrix1D(transition.rows());
		double uniform=1.0/pr.size();
		pr.assign(uniform);

		// create and initialize the weighted personalization vector
		DoubleMatrix1D wp=person.copy();
		wp.assign(Mult.mult(teleProb));

		// initialize delta and t for stopping criteria
		double delta=Double.MAX_VALUE;
		int t=0;

		// the power method
		while(t<maxT&&delta>convergence){
			DoubleMatrix1D prev=pr;
			// do the math
			pr=alg.mult(transition,pr).assign(Mult.mult(1.0-teleProb)).assign(wp,PlusMult.plusMult(1.0));
			// normalize
			MatrixUtil.normalize(pr);
			delta=MatrixUtil.getEuclideanDistance(prev,pr);
			t++;
		}
		return pr;

	}

	public static DoubleMatrix1D getPageRankVector(
			final DoubleMatrix2D transition,
			final DoubleMatrix1D person,
			double teleProb,
			int maxT,
			double convergence,
			final Collection<DoubleMatrix1D> masks
	){

		// create and initialize the PageRank rank vector
		DoubleMatrix1D pr=new DenseDoubleMatrix1D(transition.rows());
		double uniform=1.0/pr.size();
		pr.assign(uniform);

		// create and initialize the weighted personalization vector
		DoubleMatrix1D wp=person.copy();
		wp.assign(Mult.mult(teleProb));

		// initialize delta and t for stopping criteria
		double delta=Double.MAX_VALUE;
		int t=0;

		// the power method
		while(t<maxT&&delta>convergence){
			DoubleMatrix1D prev=pr;
			// do the math
			pr=alg.mult(transition,pr).assign(Mult.mult(1.0-teleProb)).assign(wp,PlusMult.plusMult(1.0));
			// normalize
			for(DoubleMatrix1D mask:masks){
				MatrixUtil.normalize(pr,mask);
			}
			delta=MatrixUtil.getEuclideanDistance(prev,pr);
			t++;
		}
		return pr;

	}

	public static double sumRow(final DoubleMatrix2D a,int row){
		double sum=0.0;
		for(int i=0;i<a.columns();i++){
			sum+=a.get(row,i);
		}
		return sum;
	}

	public static double sumColumn(final DoubleMatrix2D a,int column){
		double sum=0.0;
		for(int i=0;i<a.rows();i++){
			sum+=a.get(i,column);
		}
		return sum;
	}

	public static DoubleMatrix1D powerIteration(final DoubleMatrix2D a,int maxT,double convergence){

		if(a.rows()!=a.columns()){
			return null;
		}

		DoubleMatrix1D v=new DenseDoubleMatrix1D(a.rows());
		v.assign(1.0/v.size());

		double delta=Double.MAX_VALUE;
		for(int t=0;t<maxT&&delta>convergence;t++){
			DoubleMatrix1D prev=v;
			v=alg.mult(a,v);
			normalize(v);
			delta=getEuclideanDistance(prev,v);
		}

		return v;

	}

	public static void normalizeBy(DoubleMatrix1D v,double m){
		if(m==0){
			v.assign(0);
		}
		else{
			v.assign(Mult.div(m));
		}
	}

	public static void normalize(DoubleMatrix1D v){
		normalizeBy(v,v.zSum());
	}

	/**
	 * Normalizes only the elements that are positive in the mask
	 * 
	 * @param v the vector to be normalized
	 * @param mask the normalization mask
	 */
	public static void normalize(DoubleMatrix1D v,DoubleMatrix1D mask){
		double sum=0.0;
		for(int i=0;i<v.size();i++){
			if(mask.getQuick(i)>0.0){
				sum+=v.getQuick(i);
			}
		}
		for(int i=0;i<v.size();i++){
			if(mask.getQuick(i)>0.0){
				v.setQuick(i,v.getQuick(i)/sum);
			}
		}
	}

	public static void normalizeRows(DoubleMatrix2D a){
		for(int i=0;i<a.rows();i++){
			normalize(a.viewRow(i));
		}
	}

	public static void normalizeColumns(DoubleMatrix2D a){
		for(int i=0;i<a.columns();i++){
			normalize(a.viewColumn(i));
		}
	}
	
	// Used in Ng et al. 2001, as L_sym in Luxburg tutorial
	public static DoubleMatrix2D normalizeSymmetric(DoubleMatrix2D w){
		DoubleMatrix2D d=MatrixUtil.getDegreeMatrix(w);
		DoubleMatrix2D n=new SparseDoubleMatrix2D(w.rows(),w.columns());
		for(int i=0;i<n.rows();i++){
			for(int j=0;j<n.columns();j++){
				n.set(i,j,w.get(i,j)/(Math.sqrt(d.get(i,i))*Math.sqrt(d.get(j,j))));
			}
		}
		return n;
	}

	public static List<Integer> getOrderedValueIndexesAscend(DoubleMatrix1D values){
		ScoreTable<Integer> table=new ScoreTable<Integer>();		
		for(int i=0;i<values.size();i++){
			table.set(i,values.get(i));
		}
		return table.sortedKeysAscend();
	}

	public static List<Integer> getOrderedValueIndexesDescend(DoubleMatrix1D values){
		ScoreTable<Integer> table=new ScoreTable<Integer>();		
		for(int i=0;i<values.size();i++){
			table.set(i,values.get(i));
		}
		return table.sortedKeysDescend();
	}

	public static int getNthLargestValueIndex(DoubleMatrix1D values,int n){
		return getOrderedValueIndexesDescend(values).get(n);
	}

	public static int getNthSmallestValueIndex(DoubleMatrix1D values,int n){
		return getOrderedValueIndexesAscend(values).get(n);
	}

	public static double getMax(DoubleMatrix1D vector){
		double max=vector.getQuick(0);
		for(int i=1;i<vector.size();i++){
			if(vector.getQuick(i)>max){
				max=vector.getQuick(i);
			}
		}
		return max;
	}

	public static int getMaxIndex(DoubleMatrix1D vector){
		double max=vector.getQuick(0);
		int maxIndex=0;
		for(int i=1;i<vector.size();i++){
			if(vector.getQuick(i)>max){
				max=vector.getQuick(i);
				maxIndex=i;
			}
		}
		return maxIndex;
	}

	public static double getMin(DoubleMatrix1D vector){
		double min=vector.getQuick(0);
		for(int i=1;i<vector.size();i++){
			if(vector.getQuick(i)<min){
				min=vector.getQuick(i);
			}
		}
		return min;
	}

	public static int getMinIndex(DoubleMatrix1D vector){
		double min=vector.getQuick(0);
		int minIndex=0;
		for(int i=1;i<vector.size();i++){
			if(vector.getQuick(i)<min){
				min=vector.getQuick(i);
				minIndex=i;
			}
		}
		return minIndex;
	}

	public static final DoubleDoubleFunction ADD=new DoubleDoubleFunction(){
		@Override
		public double apply(double a,double b){
			return a+b;
		}
	};

	public static final DoubleDoubleFunction SUBTRACT=new DoubleDoubleFunction(){
		@Override
		public double apply(double a,double b){
			return a-b;
		}
	};

	public static final DoubleDoubleFunction MULTIPLY=new DoubleDoubleFunction(){
		@Override
		public double apply(double a,double b){
			return a*b;
		}
	};

	public static final DoubleDoubleFunction DIVIDE=new DoubleDoubleFunction(){
		@Override
		public double apply(double a,double b){
			return a/b;
		}
	};
	
	public static final DoubleDoubleFunction MAX=new DoubleDoubleFunction(){
		@Override
		public double apply(double a,double b){
			return a<b?b:a;
		}
	};
	
	public static final DoubleDoubleFunction MIN=new DoubleDoubleFunction(){
		@Override
		public double apply(double a,double b){
			return a>b?b:a;
		}
	};

}
