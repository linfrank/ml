package edu.cmu.cs.frank.stat.gui;
/*
 * Frank Lin
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

import edu.cmu.cs.frank.gui.GuiUtil;
import edu.cmu.cs.frank.gui.PointIcon;
import edu.cmu.cs.frank.gui.PointIconPanel;
import edu.cmu.cs.frank.stat.ChineseRestaurantProcessMixture;
import edu.cmu.cs.frank.stat.Gamma;
import edu.cmu.cs.frank.stat.Gaussian;
import edu.cmu.cs.frank.stat.GaussianMixture;
import edu.cmu.cs.frank.stat.Process;
import edu.cmu.cs.frank.stat.UniformContinuous;
import edu.cmu.cs.frank.util.IOUtil;
import edu.cmu.cs.frank.util.ParametersChooserPanel;
import edu.cmu.cs.frank.util.Parametizable;

public class RandomProcessPanel extends JPanel implements ActionListener{

	static Logger log=Logger.getLogger(RandomProcessPanel.class);

	static final long serialVersionUID=20080501L;

	private static List<Class<? extends Parametizable>> processes=new ArrayList<Class<? extends Parametizable>>();
	static{
		processes.add(Gaussian.class);
		processes.add(Gamma.class);
		processes.add(GaussianMixture.class);
		processes.add(ChineseRestaurantProcessMixture.class);
		processes.add(UniformContinuous.class);
	}
	
	public static final Process<Double> DEFAULT_Y_PROCESS=new UniformContinuous(); 

	public static final Dimension INSTANCE_PANEL_DIM=new Dimension(500,500);
	public static final Dimension CONTROL_PANEL_DIM=new Dimension(200,INSTANCE_PANEL_DIM.height);
	public static final Dimension STATUS_BAR_DIM=new Dimension(INSTANCE_PANEL_DIM.width+CONTROL_PANEL_DIM.width*2,20);
	public static final Dimension OVERALL_DIM=new Dimension(STATUS_BAR_DIM.width,INSTANCE_PANEL_DIM.height+STATUS_BAR_DIM.height);

	public static final Dimension ITEM_PREF=new Dimension(120,30);
	public static final Dimension ITEM_MAX=new Dimension(120,30);
	public static final Dimension ITEM_MIN=new Dimension(50,30);

	public static final Dimension BUTTON_PREF=new Dimension(120,30);
	public static final Dimension BUTTON_MAX=new Dimension(120,30);
	public static final Dimension BUTTON_MIN=new Dimension(80,30);

	private Process<Double> processX;
	private Process<Double> processY;

	private PointIconPanel instancePanel;

	private JTextField sampleField;
	private JButton sampleButton;
	private JButton clearButton;

	private ParametersChooserPanel pChooserPaneX;
	private ParametersChooserPanel pChooserPaneY;

	private JLabel statusBar;

	private File currDataDir;
	private File currImageDir;

	private Random random=new Random();

	public RandomProcessPanel(){

		// default dirs

		currDataDir=new File(System.getProperty("user.home"));
		currImageDir=new File(System.getProperty("user.home"));

		// data drawing area 

		instancePanel=new PointIconPanel();
		instancePanel.setBackground(Color.BLACK);
		instancePanel.setScale(true);
		instancePanel.setMonoscale(false);
		instancePanel.setPreferredSize(INSTANCE_PANEL_DIM);

		// menu bar items

		JMenu importMenu=new JMenu("Import");
		importMenu.add(new AbstractAction("From Data File"){
			static final long serialVersionUID=20080413L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currDataDir);
				int option=chooser.showOpenDialog(RandomProcessPanel.this);
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
				int option=chooser.showSaveDialog(RandomProcessPanel.this);
				if(option==JFileChooser.APPROVE_OPTION){
					File file=chooser.getSelectedFile();
					currImageDir=file.getParentFile();
					GuiUtil.writeImage(instancePanel,file,"jpeg");
				}
			}
		});
		exportMenu.add(new AbstractAction("To EPS File"){
			static final long serialVersionUID=20081001L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currImageDir);
				int option=chooser.showSaveDialog(RandomProcessPanel.this);
				if(option==JFileChooser.APPROVE_OPTION){
					File file=chooser.getSelectedFile();
					currImageDir=file.getParentFile();
					GuiUtil.writeEps(instancePanel,file);
				}
			}
		});
		exportMenu.add(new AbstractAction("To Data File"){
			static final long serialVersionUID=20080413L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currDataDir);
				int option=chooser.showSaveDialog(RandomProcessPanel.this);
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
				Color color=JColorChooser.showDialog(RandomProcessPanel.this,"Background Color",instancePanel.getBackground());
				instancePanel.setBackground(color);
				instancePanel.repaint();
			}
		});

		JMenuBar menuBar=new JMenuBar();
		menuBar.add(importMenu);
		menuBar.add(exportMenu);
		menuBar.add(optionsMenu);

		// parameters control

		pChooserPaneX=new ParametersChooserPanel(processes,"X Process",0);
		pChooserPaneX.addActionListener(this);
		pChooserPaneY=new ParametersChooserPanel(processes,"Y Process",4);
		pChooserPaneY.addActionListener(this);

		JPanel parameterControls=new JPanel();
		parameterControls.setLayout(new BoxLayout(parameterControls,BoxLayout.Y_AXIS));

		parameterControls.add(pChooserPaneX);
		parameterControls.add(pChooserPaneY);
		parameterControls.setPreferredSize(CONTROL_PANEL_DIM);

		// instance control

		JLabel sampleLabel=new JLabel("Number to Sample:");
		sampleLabel.setAlignmentX(CENTER_ALIGNMENT);

		sampleField=new JTextField("1");
		sampleField.setPreferredSize(ITEM_PREF);
		sampleField.setMaximumSize(ITEM_MAX);
		sampleField.setMinimumSize(ITEM_MIN);
		sampleField.setAlignmentX(CENTER_ALIGNMENT);
		sampleField.addActionListener(this);

		sampleButton=new JButton("Sample");
		sampleButton.setPreferredSize(BUTTON_PREF);
		sampleButton.setMaximumSize(BUTTON_MAX);
		sampleButton.setMinimumSize(BUTTON_MIN);
		sampleButton.setAlignmentX(CENTER_ALIGNMENT);
		sampleButton.addActionListener(this);

		clearButton=new JButton("Clear");
		clearButton.setPreferredSize(BUTTON_PREF);
		clearButton.setMaximumSize(BUTTON_MAX);
		clearButton.setMinimumSize(BUTTON_MIN);
		clearButton.setAlignmentX(CENTER_ALIGNMENT);
		clearButton.addActionListener(this);

		JPanel instanceControls=new JPanel();
		instanceControls.setLayout(new BoxLayout(instanceControls,BoxLayout.Y_AXIS));

		instanceControls.add(sampleLabel);
		instanceControls.add(sampleField);
		instanceControls.add(sampleButton);
		instanceControls.add(clearButton);
		instanceControls.setPreferredSize(CONTROL_PANEL_DIM);

		// status bar

		statusBar=new JLabel("Ready.");
		statusBar.setPreferredSize(STATUS_BAR_DIM);
		statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

		// overall layout

		this.setLayout(new BorderLayout());
		this.add(menuBar,BorderLayout.PAGE_START);
		this.add(parameterControls,BorderLayout.LINE_START);
		this.add(instancePanel,BorderLayout.CENTER);
		this.add(instanceControls,BorderLayout.LINE_END);
		this.add(statusBar,BorderLayout.PAGE_END);
		this.setSize(OVERALL_DIM);

	}

	private void sample(){
		double x=processX.generateNext(random);
		double y=processY.generateNext(random);
		instancePanel.addIcon(new PointIcon(new Point2D.Double(x,y),Color.RED));
		setStatus("Last sample: ("+x+", "+y+").");
	}

	private void addSample(Point2D point){
		instancePanel.addIcon(new PointIcon(point,null));
		instancePanel.repaint();
		setStatus("Point added at "+point.getX()+", "+point.getY()+" (Total = "+instancePanel.getIcons().size()+").");
	}

	@Override
	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e){
		if(e.getSource()==clearButton){
			instancePanel.clear();
			instancePanel.repaint();
			setStatus("Data points cleared.");
		}
		else if(e.getSource()==pChooserPaneX){
			processX=(Process<Double>)pChooserPaneX.getParametized();
		}
		else if(e.getSource()==pChooserPaneY){
			processY=(Process<Double>)pChooserPaneY.getParametized();
		}
		else if(e.getSource()==sampleButton){
			if(processX==null){
				processX=(Process<Double>)pChooserPaneX.getParametized();
			}
			if(processY==null){
				processY=(Process<Double>)pChooserPaneY.getParametized();
			}
			int numSamples=Integer.parseInt(sampleField.getText().trim());
			for(int i=0;i<numSamples;i++){
				sample();
			}
			instancePanel.repaint();
		}
	}

	private void setStatus(String message){
		statusBar.setText(message);
		statusBar.repaint();
	}

	private void writeInstances(File file){
		PrintWriter writer=IOUtil.getPrintWriter(file,"utf8",true);
		for(PointIcon icon:instancePanel.getIcons()){
			writer.print(icon.getPoint().getX());
			writer.print("\t");
			writer.println(icon.getPoint().getY());
		}
		writer.close();
		setStatus("Wrote data points to "+file.getName());
	}

	private void readInstances(File file){
		instancePanel.getIcons().clear();
		try{
			BufferedReader reader=IOUtil.getBufferedReader(file,"utf8");
			for(String line;(line=reader.readLine())!=null;){
				String[] tokens=line.split("\\s+");
				Point2D point=new Point2D.Double(Double.parseDouble(tokens[0]),Double.parseDouble(tokens[1]));
				addSample(point);
			}
			reader.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		instancePanel.repaint();
		setStatus("Read data points from "+file.getName());
	}

	public static void main(String[] args){

		JFrame frame=new JFrame(RandomProcessPanel.class.getSimpleName());
		frame.add(new RandomProcessPanel(),BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

}
