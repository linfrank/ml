package edu.cmu.cs.frank.ml.cluster;

import cern.colt.matrix.DoubleMatrix1D;


public interface SpectralClusterer extends ProjectionClusterer{
	
	public DoubleMatrix1D getSortedEigenvalues();

}
