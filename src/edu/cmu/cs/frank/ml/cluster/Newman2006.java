package edu.cmu.cs.frank.ml.cluster;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cern.colt.Arrays;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.classify.LabelSet;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.BinaryTree;
import edu.cmu.cs.frank.util.Parameters;

/**
 * @author Frank Lin
 * 
 * M. E. J. Newman. Modularity and community structure in networks. PNAS 2006.
 * 
 * Implementation caveat: due to precision problem with small numbers,
 * positive numbers in the indicator vector s less than the tolerance
 * are treated as zeros. The assumption here is that ambiguous instances
 * are more likely to belong to the same cluster.
 */

public class Newman2006 implements Clusterer{

	protected static Logger log=Logger.getLogger(Newman2006.class);

	public static class Params extends Parameters{

		static final long serialVersionUID=20080512L;

		public int maxClusters=1000;

		public double tolerance=1.0E-10;

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

	@Override
	public List<Label> cluster(Dataset dataset){

		log.info("Deriving affinity matrix...");

		// first derive a affinity matrix from the features
		DoubleMatrix2D a=Convert.toMatrix(dataset,Convert.Orientation.COLUMN_INSTANCE,p.dense);

		// only works on symmetric matrices
		MatrixUtil.makeSymmetricMax(a);

		log.info("Performing modularity clustering...");

		// make the top-level B matrix

		// total degree
		double m=a.zSum();
		// degree per node
		double[] d=new double[a.rows()];
		for(int i=0;i<a.rows();i++){
			d[i]=a.viewColumn(i).zSum();
		}
		// fill B
		DoubleMatrix2D b=a.like();
		for(int i=0;i<a.rows();i++){
			for(int j=0;j<a.columns();j++){
				b.setQuick(i,j,a.getQuick(i,j)-(d[i]*d[j])/m);
			}
		}

		// make a binary tree for story hierarchical clustering results
		BinaryTree<Output> results=new BinaryTree<Output>();

		// make a input queue and put the original B matrix in it
		LinkedList<Input> queue=new LinkedList<Input>();
		Input first=new Input();
		first.b=b;
		first.map=new int[b.rows()];
		for(int i=0;i<b.rows();i++){
			first.map[i]=i;
		}
		queue.add(first);

		// keep track of how many clusters
		int numClusters=1;

		// main clustering loop
		while(queue.size()>0&&numClusters<p.maxClusters){

			Input in=queue.removeFirst();

			log.info("Eigen computation for graph of size "+in.b.rows()+"...");
			EigenvalueDecomposition eigen=new EigenvalueDecomposition(in.b);
			DoubleMatrix1D reals=eigen.getRealEigenvalues();
			DoubleMatrix1D s=eigen.getV().viewColumn(MatrixUtil.getMaxIndex(reals)).copy();
			round(s);
			double v=MatrixUtil.getMax(reals);
			log.info("Leading eigenvalue: "+v);

			double mod=getModularity(in.b,s);
			log.info("Modularity Delta: "+mod);

			if(mod>p.tolerance){

				log.info("Dividing into submatrices...");

				// store results
				Output out=new Output();
				out.map=in.map;
				out.scores=s;
				out.modularity=mod;
				BinaryTree<Output> result;
				if(in.parent==null){
					result=results;
					result.setData(out);
				}
				else{
					result=new BinaryTree<Output>(out);
					if(in.left){
						in.parent.setLeft(result);
					}
					else{
						in.parent.setRight(result);
					}
				}

				// do the postive cluster first
				List<Integer> posList=new ArrayList<Integer>();
				for(int i=0;i<s.size();i++){
					if(s.getQuick(i)>p.tolerance){
						posList.add(in.map[i]);
					}
				}
				int[] posMap=ArrayUtil.toIntArray(posList);
				DoubleMatrix2D posBg=getBg(b,posMap);
				Input posInput=new Input();
				posInput.b=posBg;
				posInput.map=posMap;
				posInput.parent=result;
				posInput.left=true;
				queue.addLast(posInput);

				log.info("Positive submatrix size: "+posMap.length);

				// do the negative similarly
				List<Integer> negList=new ArrayList<Integer>();
				for(int i=0;i<s.size();i++){
					if(s.getQuick(i)<=p.tolerance){
						negList.add(in.map[i]);
					}
				}
				int[] negMap=ArrayUtil.toIntArray(negList);
				DoubleMatrix2D negBg=getBg(b,negMap);
				Input negInput=new Input();
				negInput.b=negBg;
				negInput.map=negMap;
				negInput.parent=result;
				negInput.left=false;
				queue.addLast(negInput);

				log.info("Negative submatrix size: "+negMap.length);

				numClusters++;

			}
			else{
//				if(v>p.tolerance){
//					log.warn("Good cut exists, but cut is wrong:");
//					log.warn("Problematic instances:");
//					for(int i=0;i<in.map.length;i++){
//						log.warn(i+" ["+s.get(i)+"] "+instances.get(in.map[i]).getId());
//					}
//				}
				log.info("Modularity not improved, stopping.");
			}

		}

		log.info("Finished clustering.");

//		log.info("Clustering Hierarchy:");
//		log.info(results);

		// assign clusters
		LabelSet labelSet=new LabelSet();
		Label[] labels=new Label[dataset.size()];
		assignLabels(labelSet,labels,results);

		for(int i=0;i<labels.length;i++){
			if(labels[i]==null){
				log.error("Label "+i+" is null!");
			}
		}

		log.info("Clusters: "+labelSet.size());

		return ArrayUtil.toList(labels);

	}

	private static DoubleMatrix2D getBg(DoubleMatrix2D b,int[] map){
		DoubleMatrix2D bg=b.like(map.length,map.length);
		for(int i=0;i<bg.rows();i++){
			for(int j=0;j<bg.columns();j++){
				double val=b.getQuick(map[i],map[j]);
				if(i==j){
					for(int k=0;k<bg.columns();k++){
						val-=b.getQuick(map[i],map[k]);
					}
				}
				bg.setQuick(i,j,val);
			}
		}
		return bg;
	}

	private static double getModularity(DoubleMatrix2D b,DoubleMatrix1D s){
		double m=0.0;
		for(int i=0;i<s.size();i++){
			for(int j=0;j<s.size();j++){
				if(s.getQuick(i)*s.getQuick(j)>0.0){
					m+=b.getQuick(i,j);
				}
				else{
					m-=b.getQuick(i,j);
				}
			}
		}
		return m;
	}

	private void assignLabels(LabelSet labelSet,Label[] labels,BinaryTree<Output> tree){

		// no clusters
		if(tree.getData()==null){
			String labelName="0";
			for(int i=0;i<labels.length;i++){
				labels[i]=labelSet.newLabel(labelName);
			}
		}
		else{

			// do left
			if(tree.getLeft()!=null){
				assignLabels(labelSet,labels,tree.getLeft());
			}
			else{
				String labelName=String.valueOf(labelSet.size());
				DoubleMatrix1D scores=tree.getData().scores;
				for(int i=0;i<scores.size();i++){
					if(scores.getQuick(i)>p.tolerance){
						labels[tree.getData().map[i]]=labelSet.newLabel(labelName);
					}
				}
			}

			// do right
			if(tree.getRight()!=null){
				assignLabels(labelSet,labels,tree.getRight());
			}
			else{
				String labelName=String.valueOf(labelSet.size());
				DoubleMatrix1D scores=tree.getData().scores;
				for(int i=0;i<scores.size();i++){
					if(scores.getQuick(i)<=p.tolerance){
						labels[tree.getData().map[i]]=labelSet.newLabel(labelName);
					}
				}
			}

		}

	}
	
	private void round(DoubleMatrix1D v){
		for(int i=0;i<v.size();i++){
			if(Math.abs(v.getQuick(i))<p.tolerance){
				v.setQuick(i,0.0);
			}
		}
	}

	private class Input{
		public DoubleMatrix2D b;
		public int[] map;
		public BinaryTree<Output> parent;
		public boolean left;
	}

	public class Output{

		public int[] map;
		public DoubleMatrix1D scores;
		public double modularity;

		@Override
		public String toString(){
			StringBuilder b=new StringBuilder();
			b.append(Arrays.toString(map)).append(" ").append(Arrays.toString(scores.toArray()));
			return b.toString();
		}

	}

}
