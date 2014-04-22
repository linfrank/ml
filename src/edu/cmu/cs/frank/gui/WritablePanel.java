package edu.cmu.cs.frank.gui;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;

import javax.swing.JPanel;


public class WritablePanel extends JPanel implements Printable{
	
	static final long serialVersionUID=20081102;
	
	public void writeImage(File file,String format,int width,int height){
		GuiUtil.writeImage(this,file,format,width,height);
	}
	
	public void writeImage(File file,String format){
		GuiUtil.writeImage(this,file,format);
	}

	public void writeEps(File file,int width,int height){
		GuiUtil.writeEps(this,file,width,height);
	}
	
	public void writeEps(File file){
		GuiUtil.writeEps(this,file);
	}

	@Override
	public int print(Graphics graphics,PageFormat pageFormat,int pageIndex)throws PrinterException{
		return GuiUtil.print(this,graphics,pageFormat,pageIndex);
	}

}
