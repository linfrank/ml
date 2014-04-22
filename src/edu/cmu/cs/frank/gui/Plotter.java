package edu.cmu.cs.frank.gui;
/*
 * Frank Lin
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.util.IOUtil;

public class Plotter extends JPanel{

	static Logger log=Logger.getLogger(Plotter.class);

	static final long serialVersionUID=20091130L;

	public static final Dimension INSTANCE_PANEL_DIM=new Dimension(800,600);
	public static final Dimension STATUS_BAR_DIM=new Dimension(INSTANCE_PANEL_DIM.width,20);
	public static final Dimension OVERALL_DIM=new Dimension(STATUS_BAR_DIM.width,INSTANCE_PANEL_DIM.height+STATUS_BAR_DIM.height);

	private PointIconPanel plotPanel;

	private JLabel statusBar;

	private File currDataDir;
	private File currImageDir;

	public Plotter(){

		// initialize the drawing field
		plotPanel=new PointIconPanel();
		plotPanel.setScale(true);
		plotPanel.setMonoscale(false);
		plotPanel.setPreferredSize(INSTANCE_PANEL_DIM);

		// initialize directories
		currDataDir=new File(System.getProperty("user.home"));
		currImageDir=new File(System.getProperty("user.home"));

		JMenu importMenu=new JMenu("Import");
		importMenu.add(new AbstractAction("From Data File"){
			static final long serialVersionUID=20080413L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currDataDir);
				int option=chooser.showOpenDialog(Plotter.this);
				if(option==JFileChooser.APPROVE_OPTION){
					File file=chooser.getSelectedFile();
					currDataDir=file.getParentFile();
					readInstances(file);
				}
			}
		});
		JMenu exportMenu=new JMenu("Export");
		exportMenu.add(new AbstractAction("To JPEG File"){
			static final long serialVersionUID=20081001L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currImageDir);
				int option=chooser.showSaveDialog(Plotter.this);
				if(option==JFileChooser.APPROVE_OPTION){
					File file=chooser.getSelectedFile();
					currImageDir=file.getParentFile();
					GuiUtil.writeImage(plotPanel,file,"jpeg");
				}
			}
		});
		exportMenu.add(new AbstractAction("To EPS File"){
			static final long serialVersionUID=20081001L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currImageDir);
				int option=chooser.showSaveDialog(Plotter.this);
				if(option==JFileChooser.APPROVE_OPTION){
					File file=chooser.getSelectedFile();
					currImageDir=file.getParentFile();
					GuiUtil.writeEps(plotPanel,file);
				}
			}
		});
		exportMenu.add(new AbstractAction("To Data File"){
			static final long serialVersionUID=20080413L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currDataDir);
				int option=chooser.showSaveDialog(Plotter.this);
				if(option==JFileChooser.APPROVE_OPTION){
					File file=chooser.getSelectedFile();
					currDataDir=file.getParentFile();
					writeInstances(file);
				}
			}
		});

		JMenu optionsMenu=new JMenu("Options");
		optionsMenu.add(new AbstractAction("Background Color"){
			static final long serialVersionUID=20080413L;
			@Override
			public void actionPerformed(ActionEvent e){
				Color color=JColorChooser.showDialog(Plotter.this,"Background Color",plotPanel.getBackground());
				if(color!=null){
					plotPanel.setBackground(color);
					plotPanel.repaint();
				}
			}
		});

		JMenuBar menuBar=new JMenuBar();
		menuBar.add(importMenu);
		menuBar.add(exportMenu);
		menuBar.add(optionsMenu);

		// initialize the status bar
		statusBar=new JLabel("Plotter ready.");
		statusBar.setPreferredSize(STATUS_BAR_DIM);
		statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

		this.setLayout(new BorderLayout());
		this.add(menuBar,BorderLayout.PAGE_START);
		this.add(plotPanel,BorderLayout.CENTER);
		this.add(statusBar,BorderLayout.PAGE_END);

		this.setSize(OVERALL_DIM);
	}

	private void addInstance(Point2D point){
		plotPanel.addIcon(new PointIcon(point,null));
		plotPanel.repaint();
		setStatus("Instance added at "+point.getX()+", "+point.getY()+" (Total = "+plotPanel.getIcons().size()+").");
	}

	public void mouseClicked(MouseEvent e){}

	public void mouseEntered(MouseEvent e){}

	public void mouseExited(MouseEvent e){}

	public void mousePressed(MouseEvent e){
		if(e.getButton()==MouseEvent.BUTTON1){
			addInstance(e.getPoint());
		}
	}

	public void mouseReleased(MouseEvent e){}

	public void mouseDragged(MouseEvent e){
		try{
			Thread.sleep(60);
		}
		catch(Exception x){
			x.printStackTrace();
		}
		addInstance(e.getPoint());
	}

	public void mouseMoved(MouseEvent e){}

	private void setStatus(String message){
		statusBar.setText(message);
		statusBar.repaint();
	}

	private void writeInstances(File file){
		PrintWriter writer=IOUtil.getPrintWriter(file,"utf8",true);
		for(PointIcon icon:plotPanel.getIcons()){
			writer.print(icon.getPoint().getX());
			writer.print("\t");
			writer.println(icon.getPoint().getY());
		}
		writer.close();
		setStatus("Wrote instance data to "+file.getName());
	}

	private void readInstances(File file){
		plotPanel.clear();
		try{
			BufferedReader reader=IOUtil.getBufferedReader(file,"utf8");
			int i=0;
			for(String line;(line=reader.readLine())!=null;){
				String[] tokens=line.split("\\s+");
				Point2D point;
				if(tokens.length==1){
					point=new Point2D.Double(i,Double.parseDouble(tokens[0]));
				}
				else{
					point=new Point2D.Double(Double.parseDouble(tokens[0]),Double.parseDouble(tokens[1]));
				}
				addInstance(point);
				i++;
			}
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		plotPanel.repaint();
		setStatus("Read instance data from "+file.getName());
	}

	public static void main(String[] args){

		JFrame frame=new JFrame(Plotter.class.getSimpleName());
		frame.add(new Plotter(),BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}
}
