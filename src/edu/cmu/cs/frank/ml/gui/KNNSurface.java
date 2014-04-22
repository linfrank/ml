package edu.cmu.cs.frank.ml.gui;
/*
 * Frank Lin
 * 
 * This applet draws decision surface of KNN
 * 
 */

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.cs.frank.gui.DrawUtil;
import edu.cmu.cs.frank.util.ScoreTable;

public class KNNSurface extends Applet{
	
	static final long serialVersionUID=20080501L;
	
	public static final int[][] CIRCLES={{1,3},{2,1},{2,3},{3,2},{4,5}};
	public static final int[][] SQUARES={{1,1},{1,5},{4,3},{5,2},{5,4}};
	public static final int N=1;
	public static final int SCALE=50;
	public static final int WIDTH=300; //should be the largest x * SCALE
	public static final int HEIGHT=300; //should be the largest y * SCALE
	public static final int POINT_SIZE=10;
	public static final Color POINT_COLOR=Color.BLUE;
	public static final Color CIRCLE_COLOR=Color.GREEN;
	public static final Color SQUARE_COLOR=Color.RED;
	public static final Color TIE_COLOR=Color.BLACK;
	
	private Set<Point> circles;
	private Set<Point> squares;
	
	@Override
	public void init(){
		circles=createPoints(CIRCLES);
		squares=createPoints(SQUARES);
		
		circles=transform(circles);
		squares=transform(squares);
		
		this.setSize(WIDTH,HEIGHT);
	}
	
	@Override
	public void stop(){}
	
  @Override
	public void paint(Graphics g){
  	for(int i=0;i<WIDTH;i++){
  		for(int j=0;j<HEIGHT;j++){
  			g.setColor(findColor(new Point(i,j),N));
  			g.fillRect(i,j,1,1);
  		}
  	}
  	for(Point circle:circles){
  		DrawUtil.draw(g,DrawUtil.Shape.Circle,Color.BLUE,circle.x,circle.y,POINT_SIZE,true);
  	}
  	for(Point square:squares){
  		DrawUtil.draw(g,DrawUtil.Shape.Square,Color.BLUE,square.x,square.y,POINT_SIZE,true);
  	}
  }
  
	private static Set<Point> createPoints(int[][] coords){
		Set<Point> points=new HashSet<Point>();
		for(int i=0;i<coords.length;i++){
			points.add(new Point(coords[i][0],coords[i][1]));
		}
		return points;
	}
	
  private Point transform(Point point){
		return new Point(point.x*SCALE,HEIGHT-point.y*SCALE);
	}
	
  private Set<Point> transform(Set<Point> points){
		Set<Point> newPoints=new HashSet<Point>();
		for(Point point:points){
			newPoints.add(transform(point));
		}
		return newPoints;
	}
  
	private List<Point> findNeighbors(Point point,int n){
  	ScoreTable<Point> table=new ScoreTable<Point>();
  	for(Point circle:circles){
  		table.set(circle,circle.distance(point));
  	}
  	for(Point square:squares){
  		table.set(square,square.distance(point));
  	}
  	return table.topKeys(n,true);
  }
  
	private Color findColor(Point point,int n){
  	List<Point> neighbors=findNeighbors(point,n);
  	int numCircles=0;
  	for(Point neighbor:neighbors){
  		if(circles.contains(neighbor)){
  			numCircles++;
  		}
  	}
  	if(numCircles>((double)n/2)){
  		return CIRCLE_COLOR;
  	}
  	else if(numCircles<((double)n/2)){
  		return SQUARE_COLOR;
  	}
  	else{
  		return TIE_COLOR;
  	}
  }
  
}
