package edu.cmu.cs.frank.ml.gui;
/*
 * Frank Lin
 * 
 */

import java.applet.Applet;
import java.awt.BorderLayout;

public class ClusteringApplet extends Applet{

	static final long serialVersionUID=20080501L;

	@Override
	public void init(){
		this.setLayout(new BorderLayout());
		this.add(new ClusteringPanel(),BorderLayout.CENTER);
	}

	@Override
	public void stop(){}

}
