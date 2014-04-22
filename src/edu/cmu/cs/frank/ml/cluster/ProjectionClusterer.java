package edu.cmu.cs.frank.ml.cluster;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author Frank Lin
 * 
 * Interface for clusterers that projects the data points to another space,
 * usually a lower-dimensional space, before actually clustering the points. 
 */

public interface ProjectionClusterer extends Clusterer{

	// returns a row-instance data matrix
	public DoubleMatrix2D getProjection();
	
}
