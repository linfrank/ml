package edu.cmu.cs.frank.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class DrawUtil{

	public static enum Shape{
		X,Cross,Circle,Square,Diamond,Pyramid,Funnel
	}
	
	// for drawing polygons
	private static int[] xs=new int[8];
	private static int[] ys=new int[8];

	public static void draw(Graphics g,Shape shape,Color color,int x,int y,int size,boolean fill){
		Color prevColor=g.getColor();
		g.setColor(color);
		int half=size/2;
		switch(shape){
		case X:
			g.drawLine(x-half,y-half,x+half,y+half);
			g.drawLine(x+half,y-half,x-half,y+half);
			break;
		case Cross:
			g.drawLine(x,y-half,x,y+half);
			g.drawLine(x-half,y,x+half,y);
			break;
		case Circle:
			if(fill){
				g.fillOval(x-half,y-half,size,size);
			}
			else{
				g.drawOval(x-half,y-half,size,size);
			}
			break;
		case Square:
			if(fill){
				g.fillRect(x-half,y-half,size,size);
			}
			else{
				g.drawRect(x-half,y-half,size,size);
			}
			break;
		case Diamond:
			xs[0]=x;xs[1]=x+half;xs[2]=x;xs[3]=x-half;
			ys[0]=y-half;ys[1]=y;ys[2]=y+half;ys[3]=y;
			if(fill){
				g.fillPolygon(xs,ys,4);
			}
			else{
				g.drawPolygon(xs,ys,4);
			}
			break;
		case Pyramid:
			xs[0]=x;xs[1]=x+half;xs[2]=x-half;
			ys[0]=y-half;ys[1]=y+half;ys[2]=y+half;
			if(fill){
				g.fillPolygon(xs,ys,3);
			}
			else{
				g.drawPolygon(xs,ys,3);
			}
			break;
		case Funnel:
			xs[0]=x-half;xs[1]=x+half;xs[2]=x;
			ys[0]=y-half;ys[1]=y-half;ys[2]=y+half;
			if(fill){
				g.fillPolygon(xs,ys,3);
			}
			else{
				g.drawPolygon(xs,ys,3);
			}
			break;
		default:
			System.err.println("Warning: Unkown shape "+shape);
			break;
		}
		g.setColor(prevColor);
	}

	public static void draw(Graphics g,Shape shape,Color color,int x,int y,int size){
		draw(g,shape,color,x,y,size,false);
	}

	public static void draw(Graphics g,Shape shape,Color color,Point point,int size){
		draw(g,shape,color,point.x,point.y,size);
	}

}
