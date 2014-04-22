package edu.cmu.cs.frank.ml.dimred;

import java.util.Arrays;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import edu.cmu.cs.frank.util.ArrayUtil;
import edu.cmu.cs.frank.util.Parameters;


public class SVD extends MatrixDimensionReducer{
	
	public class Params extends MatrixDimensionReducer.Params{
		
		static final long serialVersionUID=20111122L;
		
		public int dimensions=2;
		public boolean dense=false;
		
	}

	@Override
	public Parameters getParams(){
		return p;
	}

	@Override
	public void setParams(Parameters p){
		this.p=(Params)p;
	}
	
	Params p=new Params();
	
	@Override
	public DoubleMatrix2D reduce(DoubleMatrix2D full){
		
		// run SVD
		SingularValueDecomposition svd=new SingularValueDecomposition(full);
		
		// determine reduced dimensionality
		int d=Math.min(p.dimensions,svd.rank());
		
		// find most salient singular values
		int[] sorted=ArrayUtil.sortIndex(svd.getSingularValues());
		
		// create selection array
		int[] selection=Arrays.copyOf(sorted,d);
		
		// select the salient columns from the left matrix U
		DoubleMatrix2D reduced=svd.getU().viewSelection(null,selection);
		
		return reduced;
		
	}
	
}
