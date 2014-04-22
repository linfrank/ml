package edu.cmu.cs.frank.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;

import edu.cmu.cs.frank.gui.DrawUtil.Shape;


public class PointIcon{

	protected Point2D point;
	protected Color color;
	protected Shape shape;
	protected int size;

	public PointIcon(Point2D point,Color color,Shape shape,int size){
		this.point=point;
		this.color=color;
		this.shape=shape;
		this.size=size;
	}

	public PointIcon(Point2D point,Color color,Shape shape){
		this(point,color,shape,10);
	}

	public PointIcon(Point2D point,Color color){
		this(point,color,Shape.Cross);
	}

	public Point2D getPoint(){
		return point;
	}

	public void setPoint(Point point){
		this.point=point;
	}

	public Color getColor(){
		return color;
	}

	public void setColor(Color color){
		this.color=color;
	}

	public Shape getShape(){
		return shape;
	}

	public void setShape(Shape shape){
		this.shape=shape;
	}

	public int getSize(){
		return size;
	}

	public void setSize(int size){
		this.size=size;
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(point).append(" ").append(color).append(" ").append(shape).append(" ").append(size);
		return b.toString();
	}

}