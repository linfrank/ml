package edu.cmu.cs.frank.ml.graph.ssl;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.Parametizable;

public interface LabelPropagator extends Parametizable{
	
	public LPGraph getGraph();
	
	public void setGraph(LPGraph graph);
	
	public void step();
	
	public void run(int numSteps);
	
	public void run();
	
	public void reset();
	
	public void resetT();
	
	public int getT();
	
	public int getMaxT();
	
	public double getDamper();
	
	public boolean isConverged();
	
	public Logger getLogger();

}
