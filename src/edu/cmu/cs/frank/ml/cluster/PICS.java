package edu.cmu.cs.frank.ml.cluster;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.MatrixUtil;
import edu.cmu.cs.frank.util.Pair;
import edu.cmu.cs.frank.util.ScoreTable;

/**
 * @author Frank Lin
 * 
 * "Stretch" version of PIC;
 * 
 */

public class PICS extends PIC{

	protected static Logger log=Logger.getLogger(PICS.class);

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

		log.info("creating a transition matrix...");
		DoubleMatrix2D w=a.copy();
		MatrixUtil.normalizeRows(w);
		
		Random random=new Random(p.runSeed);
		
		DoubleMatrix2D proj=new DenseDoubleMatrix2D(a.rows(),p.dimensions);

		for(int i=0;i<p.dimensions;i++){

			DoubleMatrix1D initVec=getInitialVector(random,a);
				
			DoubleMatrix1D vector=iterate(w,initVec);
			
			// stretch out the projection from 0 to 1
			double stretch=1.0;
			double min=MatrixUtil.getMin(vector);
			double max=MatrixUtil.getMax(vector);
			double range=max-min;
			double factor=stretch/range;
			for(int j=0;j<vector.size();j++){
				vector.setQuick(j,(vector.getQuick(j)-min)*factor);
			}
			// density-based transformation
			double minSpace=1.0/vector.size();
			ScoreTable<Integer> values=new ScoreTable<Integer>();
			for(int j=0;j<vector.size();j++){
				values.set(j,vector.getQuick(j));
			}
			List<Integer> sorted=values.sortedKeysAscend();
			vector.setQuick(sorted.get(0),values.get(sorted.get(0)));
			for(int j=1;j<sorted.size();j++){
				int index=sorted.get(j);
				int prevIndex=sorted.get(j-1);
				double value=values.get(index);
				double prevValue=values.get(prevIndex);
				double origSpace=value-prevValue;
				// formula for space between points: (x^2+x+minSpace)/(x+1)
				double space=(origSpace*origSpace+origSpace+minSpace)/(origSpace+1.0);
				vector.setQuick(index,vector.getQuick(prevIndex)+space);
			}
			
			proj.viewColumn(i).assign(vector);
			
		}

		return proj;

	}

}
