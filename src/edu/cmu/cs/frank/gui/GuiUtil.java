package edu.cmu.cs.frank.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.UIManager;

import org.sourceforge.jlibeps.epsgraphics.EpsGraphics2D;

public class GuiUtil{

	public static void setSystemLookAndFeel(){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}catch(Exception e){
			System.out.println("Cannot detect system look and feel, using default.");
		}
	}
	
	public static void writeImage(JComponent comp,File file,String format,int width,int height){

		BufferedImage bi=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics=bi.createGraphics();
		comp.paint(graphics);
		graphics.dispose();

		try{

			// get first imagewriter available for jpeg
			ImageWriter writer=ImageIO.getImageWritersByFormatName(format).next();
			// instantiate an ImageWriteParam object with default compression options
			ImageWriteParam iwp=writer.getDefaultWriteParam();
			// set compression quality
			iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			// 1 is the highest quality
			iwp.setCompressionQuality(1);
			// write to file
			FileImageOutputStream fios=new FileImageOutputStream(file);
			writer.setOutput(fios);
			IIOImage image=new IIOImage(bi,null,null);
			writer.write(null,image,iwp);

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void writeImage(JComponent comp,File file,String format){
		writeImage(comp,file,format,comp.getWidth(),comp.getHeight());
	}
	
	public static void writeEps(JComponent comp,File file,int width,int height){

		int minX=0;
		int maxX=width;
		int minY=0;
		int maxY=height;

		try{

			FileOutputStream fos=new FileOutputStream(file);
			EpsGraphics2D g=new EpsGraphics2D("",fos,minX,minY,maxX,maxY);
			// should to turn off double buffering during paint
			boolean db=comp.isDoubleBuffered();
			comp.setDoubleBuffered(false);
			comp.paint(g);
			comp.setDoubleBuffered(db);
			g.flush();
			g.close();
			fos.close();

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void writeEps(JComponent comp,File file){
		writeEps(comp,file,comp.getWidth(),comp.getHeight());
	}

	public static int print(JComponent comp,Graphics graphics,PageFormat pageFormat,int pageIndex)throws PrinterException{
		if(pageIndex>0){
			return(Printable.NO_SUCH_PAGE);
		}else{
			Graphics2D g2d=(java.awt.Graphics2D)graphics;
			boolean db=comp.isDoubleBuffered();
			// should to turn off double buffering during paint
			comp.setDoubleBuffered(false);
			g2d.translate(pageFormat.getImageableX(),pageFormat.getImageableY());
			comp.paint(g2d);
			comp.setDoubleBuffered(db);

			return(Printable.PAGE_EXISTS);
		}
	}
	
	public static Action printAction(String actionName,final Printable printable){
		return new AbstractAction(actionName){
			static final long serialVersionUID=20081001L;
			@Override
			public void actionPerformed(ActionEvent e){
				PrinterJob printJob=PrinterJob.getPrinterJob();
				printJob.setPrintable(printable);
				if(printJob.printDialog()){
					try{
						printJob.print();
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
			}
		};
	}

}
