package edu.cmu.cs.frank.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import cern.colt.matrix.DoubleMatrix2D;


public class DoubleMatrix2DPanel extends WritablePanel{

	static final long serialVersionUID=20071218L;

	private DoubleMatrix2D matrix;
	private boolean reverse;

	public DoubleMatrix2DPanel(DoubleMatrix2D matrix,boolean reverse){
		this.matrix=matrix;
		this.reverse=reverse;
		Dimension d=new Dimension(matrix.rows(),matrix.columns());
		this.setSize(d);
		this.setPreferredSize(d);
	}

	public DoubleMatrix2DPanel(DoubleMatrix2D matrix){
		this(matrix,false);
	}
	
	public int rows(){
		return matrix.rows();
	}
	
	public int columns(){
		return matrix.columns();
	}

	public Color getColor(double value){
		if(value==0.0){
			return reverse?Color.WHITE:Color.BLACK;
		}
		else if(value==1.0){
			return reverse?Color.BLACK:Color.WHITE;
		}
		else{
			if(reverse){
				return new Color((float)(1.0-value),(float)(1.0-value),(float)(1.0-value));
			}
			else{
				return new Color((float)value,(float)value,(float)value);
			}
		}
	}

	@Override
	public void paint(Graphics g){
		for(int i=0;i<matrix.rows();i++){
			for(int j=0;j<matrix.columns();j++){
				g.setColor(getColor(matrix.get(i,j)));
				g.fillRect(j,i,1,1);
			}
		}
	}

}
