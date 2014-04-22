package edu.cmu.cs.frank.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import edu.cmu.cs.frank.util.ColorUtil;

public class PointIconPanel extends JPanel implements MouseListener,MouseMotionListener{

	static final long serialVersionUID=20080501L;

	private boolean scale=false;
	private boolean monoscale=false;
	private boolean showXTics=false;
	private boolean showYTics=false;
	private boolean showXScale=false;
	private boolean showYScale=false;
	private int scaleBorder=25;

	private double minX=Double.POSITIVE_INFINITY;
	private double minY=Double.POSITIVE_INFINITY;
	private double maxX=Double.NEGATIVE_INFINITY;
	private double maxY=Double.NEGATIVE_INFINITY;

	private PointIcon prototype=new PointIcon(null,null);

	private List<PointIcon> icons=new ArrayList<PointIcon>();
	
	private MouseListener mouseListenerOverride;

	public PointIconPanel(List<PointIcon> icons,Color background,boolean scale){
		setBackground(background);
		this.scale=scale;
		addIcons(icons);
		addMouseListener(this);
		addMouseMotionListener(this);
		ToolTipManager.sharedInstance().setInitialDelay(100);
		ToolTipManager.sharedInstance().setDismissDelay(10000);
		calculateTransform();
	}

	public PointIconPanel(List<PointIcon> icons,Color background){
		this(icons,background,false);
	}

	public PointIconPanel(List<PointIcon> icons){
		this(icons,Color.BLACK);
	}

	public PointIconPanel(){
		this(new ArrayList<PointIcon>());
	}

	private void updateMinMax(Point2D point){
		if(point.getX()<minX){
			minX=point.getX();
		}
		if(point.getY()<minY){
			minY=point.getY();
		}
		if(point.getX()>maxX){
			maxX=point.getX();
		}
		if(point.getY()>maxY){
			maxY=point.getY();
		}
	}

	private void clearMinMax(){
		minX=Double.POSITIVE_INFINITY;
		minY=Double.POSITIVE_INFINITY;
		maxX=Double.NEGATIVE_INFINITY;
		maxY=Double.NEGATIVE_INFINITY;
	}

	public void setMouseListenerOverride(MouseListener mouseListenerOverride){
		this.mouseListenerOverride=mouseListenerOverride;
	}
	
	public List<PointIcon> getIcons(){
		return icons;
	}

	public void addIcon(PointIcon icon){
		icons.add(icon);
		updateMinMax(icon.point);
	}

	public void addIcons(List<PointIcon> icons){
		for(PointIcon icon:icons){
			addIcon(icon);
		}
	}

	public void setPrototype(PointIcon prototype){
		this.prototype=prototype;
	}

	public void clear(){
		icons.clear();
		clearMinMax();
		if(scale){
			calculateTransform();
		}
	}

	private void addClick(Point2D point){
		if(scale){
			point=new Point2D.Double((point.getX()-transX)/scaleX,(point.getY()-transY)/scaleY);
		}
		addIcon(new PointIcon(point,prototype.color,prototype.shape,prototype.size));
		repaint();
	}

	private double transX;
	private double transY;
	private double scaleX;
	private double scaleY;

	private void calculateTransform(){
//		System.out.println("transformation:");
//		System.out.println("minX="+minX);
//		System.out.println("maxX="+maxX);
//		System.out.println("minY="+minY);
//		System.out.println("maxY="+maxY);
		if(icons.size()<1){
			scaleX=1.0;
			scaleY=1.0;
			transX=0.0;
			transY=0.0;
		}
		else{
			double rangeX;
			double rangeY;
			if(icons.size()==1){
				rangeX=Math.abs(maxX);
				rangeY=Math.abs(maxY);
			}
			else{
				rangeX=maxX-minX;
				rangeY=maxY-minY;
			}
//			System.out.println("rangeX="+rangeX);
//			System.out.println("rangeY="+rangeY);
			scaleX=(getWidth()-scaleBorder*2)/rangeX;
			scaleY=(getHeight()-scaleBorder*2)/rangeY;
			if(monoscale){
				double maxscale=rangeX>rangeY?scaleX:scaleY;
				scaleX=maxscale;
				scaleY=maxscale;
			}
			transX=-minX*scaleX+scaleBorder;
			transY=-minY*scaleY+scaleBorder;
		}
//		System.out.println("scaleX="+scaleX);
//		System.out.println("scaleY="+scaleY);
//		System.out.println("transX="+transX);
//		System.out.println("transY="+transY);
	}

	@Override
	public void paint(Graphics g){

		super.paint(g);

		Color neg=ColorUtil.getRGBNegative(getBackground());

		// draw points scaled
		if(scale){
			calculateTransform();
			for(PointIcon icon:icons){
				int x=(int)(icon.point.getX()*scaleX+transX);
				int y=(int)(icon.point.getY()*scaleY+transY);
				Color iconColor=icon.color==null?neg:icon.color;
				DrawUtil.draw(g,icon.shape,iconColor,new Point(x,y),icon.size);
			}
		}
		// draw points non-scaled
		else{
			for(PointIcon icon:icons){
				Color iconColor=icon.color==null?neg:icon.color;
				DrawUtil.draw(g,icon.shape,iconColor,(int)icon.point.getX(),(int)icon.point.getY(),icon.size);
			}
		}

		Color prevColor=g.getColor();
		g.setColor(neg);

		if(showXTics){
			g.drawString("x max="+maxX,getWidth()-20,getHeight()-5);
			g.drawString("x min="+minX,10,getHeight()-5);
		}

		if(showYTics){
			g.drawString("y max="+maxY,10,getHeight()-5);
			g.drawString("y min="+minY,10,15);
		}

		if(showXScale){
			g.drawString("x scale="+(maxX-minX),getWidth()/2,getHeight()-5);
		}

		if(showYScale){
			((Graphics2D)g).translate(getWidth()-5,getHeight()/2);
			((Graphics2D)g).rotate(-Math.PI/2.0);
			g.drawString("y scale="+(maxY-minY),0,0);
			((Graphics2D)g).translate(0,0);
			((Graphics2D)g).rotate(0);
		}

		g.setColor(prevColor);

	}

	public void setScale(boolean scale){
		this.scale=scale;
	}

	public void setMonoscale(boolean monoscale){
		this.monoscale=monoscale;
	}

	public void setShowXTics(boolean showXTics){
		this.showXTics=showXTics;
	}

	public void setShowYTics(boolean showYTics){
		this.showYTics=showYTics;
	}

	public void setShowXScale(boolean showXScale){
		this.showXScale=showXScale;
	}

	public void setShowYScale(boolean showYScale){
		this.showYScale=showYScale;
	}

	public void setScaleBorder(int scaleBorder){
		this.scaleBorder=scaleBorder;
	}

	@Override
	public void mouseClicked(MouseEvent e){}

	@Override
	public void mouseEntered(MouseEvent e){}

	@Override
	public void mouseExited(MouseEvent e){}

	@Override
	public void mousePressed(MouseEvent e){
		if(mouseListenerOverride==null){
			addClick(e.getPoint());
		}
		else{
			mouseListenerOverride.mousePressed(e);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e){}

	@Override
	public void mouseDragged(MouseEvent e){
		try{
			Thread.sleep(60);
		}
		catch(Exception x){
			x.printStackTrace();
		}
		addClick(e.getPoint());
	}

	@Override
	public void mouseMoved(MouseEvent e){
		this.setToolTipText("x="+((e.getX()-transX)/scaleX)+", y="+((e.getY()-transY))/scaleY);
	}

}
