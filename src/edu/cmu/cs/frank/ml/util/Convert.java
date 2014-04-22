package edu.cmu.cs.frank.ml.util;

import java.util.Collection;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.graph.LabelSticky;
import edu.cmu.cs.frank.ml.graph.WeightedSticky;
import edu.cmu.cs.frank.ml.graph.ssl.LPGraph;
import edu.cmu.cs.frank.ml.graph.ssl.LPVertex;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;


public class Convert{

	static Logger log=Logger.getLogger(Convert.class);

	/**
	 * Converts a Dataset to a JUNG graph, assuming feature names map to Instance IDs.
	 * All instances without IDs will be ignored.
	 * 
	 * @param dataset
	 * @param directed
	 * @param addNonInstanceNodes
	 * @return
	 */

	public static Graph<LabelSticky,WeightedSticky> toJungGraph(Dataset dataset,boolean directed,boolean addNonInstanceNodes){

		log.info("Loading "+dataset.size()+" instances...");

		Graph<LabelSticky,WeightedSticky> graph;
		if(directed){
			graph=new DirectedSparseGraph<LabelSticky,WeightedSticky>();
		}
		else{
			graph=new UndirectedSparseGraph<LabelSticky,WeightedSticky>();
		}

		for(int i=0;i<dataset.size();i++){
			Instance instance=dataset.getInstance(i);
			LabelSticky from=new LabelSticky(instance.getId(),instance.getLabel());
			graph.addVertex(from);
			for(String feature:instance.getNonzeroFeatureNames()){
				if(addNonInstanceNodes||dataset.contains(feature)){
					LabelSticky to=new LabelSticky(feature,dataset.getInstance(feature).getLabel());
					graph.addVertex(to);
					graph.addEdge(new WeightedSticky(graph.getEdgeCount(),instance.getFeatureValue(feature)),from,to);
				}
			}
		}

		log.info("Loaded "+graph.getVertexCount()+" vertices and "+graph.getEdgeCount()+" edges.");
		return graph;

	}
	
	/**
	 * Converts a JUNG graph into a Dataset. The indexing of instances and features are aligned.
	 *
	 * @param graph a JUNG graph object
	 * @return an index-aligned Dataset object
	 */

	public static Dataset toDataset(Graph<LabelSticky,WeightedSticky> graph){
		
		// create dataset
		Dataset dataset=new Dataset();
		
		// get an arbitrary traversal order
		Collection<LabelSticky> vertices=graph.getVertices();
		
		// first fill the feature set in order
		for(LabelSticky v:vertices){
			dataset.getFeatureSet().index(v.id.toString());
		}
		// then add instances in the same order; instance and feature indexes should now align
		for(LabelSticky v:vertices){
			Instance instance=dataset.newInstance(v.id.toString());
			if(v.label!=null){
				if(dataset.getLabelSet().size()<1){
					dataset.setLabelSet(v.label.getSet());
				}
				else if(dataset.getLabelSet()!=v.label.getSet()){
					log.error("Encountered mismatching label sets");
					return null;
				}
				instance.setLabel(v.label);
			}
			for(LabelSticky s:graph.getSuccessors(v)){
				instance.setFeature(s.id.toString(),graph.findEdge(v,s).weight);
			}
		}
		return dataset;
	}
	
	public static Dataset toConnectedDataset(Dataset dataset,boolean directed,boolean addNonInstanceNodes){
		Graph<LabelSticky,WeightedSticky> graph=toJungGraph(dataset,directed,addNonInstanceNodes);
		graph=DataUtil.getLCC(graph);
		return toDataset(graph);
	}
	
	public static Dataset toDataset(LPGraph graph){

		Dataset dataset=new Dataset();

		for(LPVertex vertex:graph.getVertices()){
			Instance instance=dataset.newInstance(vertex.id.toString());
			for(LPVertex neighbor:graph.getNeighbors(vertex)){
				instance.setFeature(neighbor.id.toString(),graph.findEdge(vertex,neighbor).weight);
			}
			dataset.add(instance);
		}

		return dataset;

	}

	public static Dataset toCosineSimDataset(Dataset original){

		Dataset dataset=new Dataset(original.getLabelSet());

		log.info("Converting instances to vectors...");

		DoubleMatrix1D[] vecs=new DoubleMatrix1D[original.size()];
		for(int i=0;i<original.size();i++){
			vecs[i]=Convert.toVector(original.getInstance(i));
		}

		log.info("Calculating norms...");

		Algebra alg=new Algebra();
		double[] norms=new double[vecs.length];
		for(int i=0;i<norms.length;i++){
			norms[i]=alg.norm2(vecs[i]);
		}

		log.info("Calculating dot products...");

		double[][] dps=new double[vecs.length][vecs.length];
		for(int i=0;i<dps.length;i++){
			for(int j=0;j<dps[i].length;j++){
				if(i<j){
					dps[i][j]=MatrixUtil.getDotProduct(vecs[i],vecs[j]);
				}
				else if(i>j){
					dps[i][j]=dps[j][i];
				}
				else{
					dps[i][j]=0;
				}
			}
		}

		log.info("Calculating similarity for new dataset...");

		for(int i=0;i<original.size();i++){
			Instance oi=original.getInstance(i);
			Instance ni=dataset.newInstance(oi.getId());
			ni.setLabel(oi.getLabel());
			for(int j=0;j<original.size();j++){
				if(dps[i][j]>0){
					ni.setFeature(original.getInstance(j).getId(),dps[i][j]/(norms[i]*norms[j]));
				}
			}
		}

		return dataset;

	}

	public static DoubleMatrix1D toVector(Instance instance,boolean dense){
		int size=instance.getFeatureSet().size();
		DoubleMatrix1D vector=dense?new DenseDoubleMatrix1D(size):new SparseDoubleMatrix1D(size);
		for(Entry<Integer,Double> feature:instance.getFeatures().entrySet()){
			vector.setQuick(feature.getKey(),feature.getValue());
		}
		return vector;
	}

	public static DoubleMatrix1D toVector(Instance instance){
		return toVector(instance,false);
	}

	public enum Orientation {ROW_INSTANCE,COLUMN_INSTANCE};

	public static DoubleMatrix2D toMatrix(Dataset dataset,Orientation orient,boolean dense){
		int rows;
		int cols;
		if(orient.equals(Orientation.ROW_INSTANCE)){
			rows=dataset.size();
			cols=dataset.getFeatureSet().size();
		}
		else{
			rows=dataset.getFeatureSet().size();
			cols=dataset.size();
		}
		DoubleMatrix2D a=dense?new DenseDoubleMatrix2D(rows,cols):new SparseDoubleMatrix2D(rows,cols);
		for(int i=0;i<dataset.size();i++){
			for(Entry<Integer,Double> feature:dataset.getInstance(i).getFeatures().entrySet()){
				if(orient.equals(Orientation.ROW_INSTANCE)){
					a.setQuick(i,feature.getKey(),feature.getValue());
				}
				else{
					a.setQuick(feature.getKey(),i,feature.getValue());
				}
			}
		}
		return a;
	}

	public static DoubleMatrix2D toMatrix(Dataset dataset,Orientation orient){
		return toMatrix(dataset,orient,false);
	}

	public static DoubleMatrix2D toAffinityMatrix(Dataset dataset,Orientation orient,boolean dense,boolean directed,boolean addNonInstanceNodes){
		dataset=toDataset(toJungGraph(dataset,directed,addNonInstanceNodes));
		return toMatrix(dataset,orient,dense);
	}

}
