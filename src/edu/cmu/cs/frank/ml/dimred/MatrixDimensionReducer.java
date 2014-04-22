package edu.cmu.cs.frank.ml.dimred;

import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.util.Convert;
import edu.cmu.cs.frank.ml.util.Convert.Orientation;
import edu.cmu.cs.frank.util.Parameters;


public abstract class MatrixDimensionReducer implements DimensionReducer{
	
	public static class Params extends Parameters{
		
		static final long serialVersionUID=20111122L;

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
	public Dataset reduce(Dataset full){
		
		// convert to matrix
		DoubleMatrix2D m=Convert.toMatrix(full,Orientation.ROW_INSTANCE,p.dense);
		
		// reduce
		DoubleMatrix2D r=reduce(m);
		
		// create reduced dimension dataset
		Dataset reduced=new Dataset();
		for(int i=0;i<r.rows();i++){
			Instance instance=reduced.newInstance(full.getInstance(i).getId());
			for(int j=0;j<r.columns();j++){
				double val=r.getQuick(i,j);
				if(val!=0.0){
					instance.setFeature(String.valueOf(j),val);
				}
			}
		}
		
		return reduced;
		
	}
	
	public abstract DoubleMatrix2D reduce(DoubleMatrix2D full);
	
}
