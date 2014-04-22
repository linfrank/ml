package edu.cmu.cs.frank.ml.gui;
/*
 * Frank Lin
 * 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

import org.apache.log4j.Logger;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import edu.cmu.cs.frank.gui.DrawUtil.Shape;
import edu.cmu.cs.frank.gui.GuiUtil;
import edu.cmu.cs.frank.gui.PointIcon;
import edu.cmu.cs.frank.gui.PointIconPanel;
import edu.cmu.cs.frank.ml.classify.Dataset;
import edu.cmu.cs.frank.ml.classify.Instance;
import edu.cmu.cs.frank.ml.classify.Label;
import edu.cmu.cs.frank.ml.cluster.Clusterer;
import edu.cmu.cs.frank.ml.cluster.GMeans;
import edu.cmu.cs.frank.ml.cluster.KMeans;
import edu.cmu.cs.frank.ml.cluster.KWalks;
import edu.cmu.cs.frank.ml.cluster.KWalksExp;
import edu.cmu.cs.frank.ml.cluster.KWalksFast;
import edu.cmu.cs.frank.ml.cluster.Newman2006;
import edu.cmu.cs.frank.ml.cluster.NormalizedCut;
import edu.cmu.cs.frank.ml.cluster.PIC;
import edu.cmu.cs.frank.ml.cluster.PICD;
import edu.cmu.cs.frank.ml.cluster.PICH;
import edu.cmu.cs.frank.ml.cluster.ProjectionClusterer;
import edu.cmu.cs.frank.ml.cluster.Spectral;
import edu.cmu.cs.frank.ml.cluster.SpectralClusterer;
import edu.cmu.cs.frank.ml.cluster.sim.ElectronSim;
import edu.cmu.cs.frank.ml.cluster.sim.EpsilonSim;
import edu.cmu.cs.frank.ml.cluster.sim.EuclideanSim;
import edu.cmu.cs.frank.ml.cluster.sim.GaussianSim;
import edu.cmu.cs.frank.ml.cluster.sim.Similarity;
import edu.cmu.cs.frank.stat.Gaussian;
import edu.cmu.cs.frank.util.ColorUtil;
import edu.cmu.cs.frank.util.IOUtil;
import edu.cmu.cs.frank.util.Parameters;
import edu.cmu.cs.frank.util.ParametersPanel;

public class ClusteringPanel extends JPanel implements ActionListener,ItemListener,ListCellRenderer,MouseListener{

	static Logger log=Logger.getLogger(ClusteringPanel.class);

	static final long serialVersionUID=20111004L;

	private static List<Class<? extends Similarity>> sims=new ArrayList<Class<? extends Similarity>>();
	static{
		sims.add(GaussianSim.class);
		sims.add(EpsilonSim.class);
		sims.add(EuclideanSim.class);
		sims.add(ElectronSim.class);
	}

	private static List<Class<? extends Clusterer>> algorithms=new ArrayList<Class<? extends Clusterer>>();
	static{
		algorithms.add(KMeans.class);
		algorithms.add(NormalizedCut.class);
		algorithms.add(Spectral.class);
		algorithms.add(KWalks.class);
		algorithms.add(KWalksFast.class);
		algorithms.add(KWalksExp.class);
		algorithms.add(Newman2006.class);
		algorithms.add(PIC.class);
		algorithms.add(PICH.class);
		algorithms.add(PICD.class);
		algorithms.add(GMeans.class);
	}

	public static final String TOY_DATA_PATH="toydata/2d";

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

	public static final int DEFAULT_RANDOM=10;
	public static final int DEFAULT_GAUSSIAN=25;
	public static final int DEFAULT_G_SIGMA=3;

	private static DefaultListCellRenderer defaultListCellRenderer=new DefaultListCellRenderer();

	private JTabbedPane tabPane;
	private PointIconPanel instancePanel;

	private JCheckBox ticCheck;
	private JTextField randomField;
	private JButton randomButton;
	private JTextField gaussianField;
	private JTextField gSigmaField;
	private JButton clearButton;

	private JComboBox simChooser;
	private JScrollPane simScrollPane;

	private JComboBox algorithmChooser;
	private JScrollPane paramsScrollPane;

	private JButton clusterButton;

	private JLabel statusBar;

	private File currDataDir;
	private File currImageDir;

	private Random rand=new Random();

	public ClusteringPanel(){

		// initialize the drawing field
		instancePanel=new PointIconPanel();
		instancePanel.setPreferredSize(INSTANCE_PANEL_DIM);
		instancePanel.setMouseListenerOverride(this);

		// initilized tabbed pane
		tabPane=new JTabbedPane();
		tabPane.addTab("Instances",instancePanel);

		// initialize the control panel

		currDataDir=new File(System.getProperty("user.home"));
		currImageDir=new File(System.getProperty("user.home"));

		JMenu loadMenu=new JMenu("Load");
		loadMenu.add(new AbstractAction("Data File..."){
			static final long serialVersionUID=20080413L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currDataDir);
				int option=chooser.showOpenDialog(ClusteringPanel.this);
				if(option==JFileChooser.APPROVE_OPTION){
					File file=chooser.getSelectedFile();
					currDataDir=file.getParentFile();
					readInstances(file);
				}
			}
		});
		List<String> filenames=getToyDataList(TOY_DATA_PATH);
		if(filenames.size()>0){
			loadMenu.addSeparator();
			for(String filename:filenames){
				loadMenu.add(new AbstractAction(filename){
					static final long serialVersionUID=20111004L;
					@Override
					public void actionPerformed(ActionEvent e){
						File file=new File(ClassLoader.getSystemResource(TOY_DATA_PATH+"/"+e.getActionCommand()).getFile());
						readInstances(file);
					}
				});
			}
		}

		JMenu saveMenu=new JMenu("Save");
		saveMenu.add(new AbstractAction("Data File..."){
			static final long serialVersionUID=20080413L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currDataDir);
				int option=chooser.showSaveDialog(ClusteringPanel.this);
				if(option==JFileChooser.APPROVE_OPTION){
					File file=chooser.getSelectedFile();
					currDataDir=file.getParentFile();
					writeInstances(file);
				}
			}
		});
		JMenu saveImageMenu=new JMenu("Image");
		saveMenu.add(saveImageMenu);
		saveImageMenu.add(new AbstractAction("To JPEG File..."){
			static final long serialVersionUID=20081001L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currImageDir);
				int option=chooser.showSaveDialog(ClusteringPanel.this);
				if(option==JFileChooser.APPROVE_OPTION){
					File file=chooser.getSelectedFile();
					currImageDir=file.getParentFile();
					GuiUtil.writeImage((JComponent)tabPane.getSelectedComponent(),file,"jpeg");
				}
			}
		});
		saveImageMenu.add(new AbstractAction("To EPS File..."){
			static final long serialVersionUID=20081001L;
			@Override
			public void actionPerformed(ActionEvent e){
				JFileChooser chooser=new JFileChooser(currImageDir);
				int option=chooser.showSaveDialog(ClusteringPanel.this);
				if(option==JFileChooser.APPROVE_OPTION){
					File file=chooser.getSelectedFile();
					currImageDir=file.getParentFile();
					GuiUtil.writeEps((JComponent)tabPane.getSelectedComponent(),file);
				}
			}
		});

		JMenu optionsMenu=new JMenu("Options");
		optionsMenu.add(new AbstractAction("Background Color"){
			static final long serialVersionUID=20080413L;
			@Override
			public void actionPerformed(ActionEvent e){
				Color color=JColorChooser.showDialog(ClusteringPanel.this,"Background Color",instancePanel.getBackground());
				if(color!=null){
					for(int i=0;i<tabPane.getTabCount();i++){
						tabPane.getComponentAt(i).setBackground(color);
						tabPane.getComponentAt(i).repaint();
					}
				}
			}
		});

		JMenuBar menuBar=new JMenuBar();
		menuBar.add(loadMenu);
		menuBar.add(saveMenu);
		menuBar.add(optionsMenu);

		ticCheck=new JCheckBox("View Tics");
		ticCheck.setSelected(true);
		ticCheck.setAlignmentX(CENTER_ALIGNMENT);
		ticCheck.addItemListener(this);

		JLabel randomLabel=new JLabel("Random Points:");
		randomLabel.setAlignmentX(CENTER_ALIGNMENT);

		randomField=new JTextField(String.valueOf(DEFAULT_RANDOM));
		randomField.setPreferredSize(ITEM_PREF);
		randomField.setMaximumSize(ITEM_MAX);
		randomField.setMinimumSize(ITEM_MIN);
		randomField.setAlignmentX(CENTER_ALIGNMENT);
		randomField.addActionListener(this);

		randomButton=new JButton("Add Random");
		randomButton.setPreferredSize(BUTTON_PREF);
		randomButton.setMaximumSize(BUTTON_MAX);
		randomButton.setMinimumSize(BUTTON_MIN);
		randomButton.setAlignmentX(CENTER_ALIGNMENT);
		randomButton.addActionListener(this);

		JLabel gaussianLabel=new JLabel("Gaussian Points:");
		gaussianLabel.setAlignmentX(CENTER_ALIGNMENT);

		gaussianField=new JTextField(String.valueOf(DEFAULT_GAUSSIAN));
		gaussianField.setPreferredSize(ITEM_PREF);
		gaussianField.setMaximumSize(ITEM_MAX);
		gaussianField.setMinimumSize(ITEM_MIN);
		gaussianField.setAlignmentX(CENTER_ALIGNMENT);
		gaussianField.addActionListener(this);

		JLabel gSigmaLabel=new JLabel("Gaussian \u03c3:");
		gSigmaLabel.setAlignmentX(CENTER_ALIGNMENT);

		gSigmaField=new JTextField(String.valueOf(DEFAULT_G_SIGMA));
		gSigmaField.setPreferredSize(ITEM_PREF);
		gSigmaField.setMaximumSize(ITEM_MAX);
		gSigmaField.setMinimumSize(ITEM_MIN);
		gSigmaField.setAlignmentX(CENTER_ALIGNMENT);
		gSigmaField.addActionListener(this);

		clearButton=new JButton("Clear");
		clearButton.setPreferredSize(BUTTON_PREF);
		clearButton.setMaximumSize(BUTTON_MAX);
		clearButton.setMinimumSize(BUTTON_MIN);
		clearButton.setAlignmentX(CENTER_ALIGNMENT);
		clearButton.addActionListener(this);

		JLabel simLabel=new JLabel("Similarity:");
		simLabel.setAlignmentX(CENTER_ALIGNMENT);

		simChooser=new JComboBox(new Vector<Class<? extends Similarity>>(sims));
		simChooser.setPreferredSize(ITEM_PREF);
		simChooser.setMaximumSize(ITEM_MAX);
		simChooser.setMinimumSize(ITEM_MIN);
		simChooser.setAlignmentX(CENTER_ALIGNMENT);
		simChooser.setRenderer(this);
		simChooser.addActionListener(this);

		simScrollPane=new JScrollPane();
		displaySimPanel();

		JPanel instanceControls=new JPanel();
		instanceControls.setLayout(new BoxLayout(instanceControls,BoxLayout.Y_AXIS));

		instanceControls.add(ticCheck);
		instanceControls.add(randomLabel);
		instanceControls.add(randomField);
		instanceControls.add(randomButton);
		instanceControls.add(gaussianLabel);
		instanceControls.add(gaussianField);
		instanceControls.add(gSigmaLabel);
		instanceControls.add(gSigmaField);
		instanceControls.add(clearButton);
		instanceControls.add(new JSeparator(SwingConstants.HORIZONTAL));
		instanceControls.add(simLabel);
		instanceControls.add(simChooser);
		instanceControls.add(simScrollPane);

		instanceControls.setPreferredSize(CONTROL_PANEL_DIM);

		JLabel algorithmLabel=new JLabel("Algorithm:");
		algorithmLabel.setAlignmentX(CENTER_ALIGNMENT);

		algorithmChooser=new JComboBox(new Vector<Class<? extends Clusterer>>(algorithms));
		algorithmChooser.setPreferredSize(ITEM_PREF);
		algorithmChooser.setMaximumSize(ITEM_MAX);
		algorithmChooser.setMinimumSize(ITEM_MIN);
		algorithmChooser.setAlignmentX(CENTER_ALIGNMENT);
		algorithmChooser.setRenderer(this);
		algorithmChooser.addActionListener(this);

		paramsScrollPane=new JScrollPane();
		displayParametersPanel();

		clusterButton=new JButton("Cluster");
		clusterButton.setPreferredSize(BUTTON_PREF);
		clusterButton.setMaximumSize(BUTTON_MAX);
		clusterButton.setMinimumSize(BUTTON_MIN);
		clusterButton.setAlignmentX(CENTER_ALIGNMENT);
		clusterButton.addActionListener(this);

		JPanel clusterControls=new JPanel();
		clusterControls.setLayout(new BoxLayout(clusterControls,BoxLayout.Y_AXIS));

		clusterControls.add(algorithmLabel);
		clusterControls.add(algorithmChooser);
		clusterControls.add(paramsScrollPane);
		clusterControls.add(clusterButton);
		clusterControls.setPreferredSize(CONTROL_PANEL_DIM);

		// initialize the status bar
		statusBar=new JLabel("Clustering demo ready.");
		statusBar.setPreferredSize(STATUS_BAR_DIM);
		statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

		this.setLayout(new BorderLayout());
		this.add(menuBar,BorderLayout.PAGE_START);
		this.add(instanceControls,BorderLayout.LINE_START);
		this.add(tabPane,BorderLayout.CENTER);
		this.add(clusterControls,BorderLayout.LINE_END);
		this.add(statusBar,BorderLayout.PAGE_END);

		this.setSize(OVERALL_DIM);
	}

	private void run(Class<Clusterer> clazz,ParametersPanel paramsPanel,ParametersPanel simPanel){

		while(tabPane.getTabCount()>1){
			tabPane.remove(1);
		}

		paramsPanel.getParameters().defineLowerBound("k",2,true);
		paramsPanel.getParameters().defineUpperBound("k",instancePanel.getIcons().size(),true);

		boolean useSim;

		Clusterer clusterer;

		if(clazz.equals(KMeans.class)){
			clusterer=new KMeans();
			useSim=false;
		}
		else if(clazz.equals(GMeans.class)){
			clusterer=new GMeans();
			useSim=false;
		}
		else if(clazz.equals(NormalizedCut.class)){
			paramsPanel.getParameters().defineUpperBound("k",2,true);
			clusterer=new NormalizedCut();
			useSim=true;
		}
		else if(algorithms.contains(clazz)){
			try{
				clusterer=clazz.newInstance();
			}catch(InstantiationException ie){
				ie.printStackTrace();
				return;
			}catch(IllegalAccessException iae){
				iae.printStackTrace();
				return;
			}
			useSim=true;
		}
		else{
			setStatus("Unknown clustering algorithm: "+clazz);
			clusterer=null;
			useSim=false;
			return;
		}

		if(!paramsPanel.parseParameters()||!simPanel.parseParameters()){
			return;
		}
		clusterer.setParams(paramsPanel.getParameters());
		long time=System.currentTimeMillis();
		setStatus("Transforming points into instances...");
		Dataset dataset=transformPoints(useSim,(Similarity)Parameters.newInstance(simPanel.getParameters()));
		Map<Instance,PointIcon> iconMap=new HashMap<Instance,PointIcon>();
		for(int i=0;i<dataset.size();i++){
			iconMap.put(dataset.getInstance(i),instancePanel.getIcons().get(i));
		}
		setStatus("Running clustering algorithm...");
		List<Label> labels=clusterer.cluster(dataset);
		Color[] scheme=ColorUtil.getHSBWheelScheme(labels.get(0).getSet().size());
		Shape[] shapes=Shape.values();
		for(int i=0;i<labels.size();i++){
			PointIcon icon=iconMap.get(dataset.getInstance(i));
			int bestId=labels.get(i).getBestId();
			icon.setColor(scheme[bestId]);
			icon.setShape(shapes[bestId%shapes.length]);
		}
		instancePanel.repaint();
		if(clusterer instanceof ProjectionClusterer){
			DoubleMatrix2D projection=((ProjectionClusterer)clusterer).getProjection();
			for(int i=0;i<projection.columns();i++){
				PointIconPanel plotPanel=new PointIconPanel();
				plotPanel.setScale(true);
				plotPanel.setBackground(instancePanel.getBackground());
				plotPanel.setShowYTics(ticCheck.isSelected());
				for(int j=0;j<projection.rows();j++){
					Point2D point=new Point2D.Double(j,projection.getQuick(j,i));
					Color color=scheme[labels.get(j).getBestId()];
					plotPanel.addIcon(new PointIcon(point,color,Shape.Circle,5));
				}
				tabPane.addTab("x"+(i+1),plotPanel);
			}
			if(clusterer instanceof SpectralClusterer){
				// most "significant" eigenvalues
				DoubleMatrix1D eigenvalues=((SpectralClusterer)clusterer).getSortedEigenvalues();
				PointIconPanel eigenvaluesPanel=new PointIconPanel();
				eigenvaluesPanel.setScale(true);
				eigenvaluesPanel.setBackground(instancePanel.getBackground());
				eigenvaluesPanel.setShowYTics(ticCheck.isSelected());
				int numValues=Math.min(eigenvalues.size(),10);
				for(int i=0;i<numValues;i++){
					eigenvaluesPanel.addIcon(new PointIcon(new Point2D.Double(i,eigenvalues.getQuick(i)),null,Shape.Circle,5));
				}
				tabPane.addTab("\u03bb (top "+numValues+")",eigenvaluesPanel);
			}
		}
		setStatus("Finished clustering "+dataset.size()+" points in "+(System.currentTimeMillis()-time)+" ms.");
	}

	private Dataset transformPoints(boolean useSim,Similarity sim){

		Dataset dataset=new Dataset();
		List<PointIcon> icons=instancePanel.getIcons();

		// create a x-y coordinate dataset
		for(int i=0;i<icons.size();i++){
			Point2D point=icons.get(i).getPoint();
			String id=i+":"+point.getX()+","+point.getY();
			Instance instance=dataset.newInstance(id);
			instance.setFeature("x",point.getX());
			instance.setFeature("y",point.getY());
		}

		if(useSim){
			// create a similarity dataset
			Dataset simDataset=new Dataset();
			// first register features in order for an affinity matrix
			for(int i=0;i<dataset.size();i++){
				simDataset.newInstance(dataset.getInstance(i).getId());
				simDataset.getFeatureSet().index(String.valueOf(i));
			}
			// fill the matrix
			for(int i=0;i<simDataset.size();i++){
				for(int j=0;j<i;j++){
					double s=sim.measure(dataset.getInstance(i),dataset.getInstance(j));
					simDataset.getInstance(i).setFeature(String.valueOf(j),s);
					simDataset.getInstance(j).setFeature(String.valueOf(i),s);
				}
			}
			return simDataset;
		}
		else{
			return dataset;
		}

	}

	@SuppressWarnings("unchecked")
	private void displaySimPanel(){
		ParametersPanel simPanel=((ParametersPanel)simScrollPane.getViewport().getView());
		if(simPanel!=null){
			simPanel.removeActionListener(this);
		}
		Class<Similarity> clazz=(Class<Similarity>)simChooser.getSelectedItem();
		simPanel=new ParametersPanel(Parameters.getDefault(clazz),clazz.getSimpleName());
		simPanel.addActionListener(this);
		simScrollPane.setViewportView(simPanel);
		simPanel.setSize(simScrollPane.getViewport().getViewSize());
		simPanel.revalidate();
	}

	@SuppressWarnings("unchecked")
	private void displayParametersPanel(){
		ParametersPanel paramsPanel=((ParametersPanel)paramsScrollPane.getViewport().getView());
		if(paramsPanel!=null){
			paramsPanel.removeActionListener(this);
		}
		Class<Clusterer> clazz=(Class<Clusterer>)algorithmChooser.getSelectedItem();
		paramsPanel=new ParametersPanel(Parameters.getDefault(clazz),clazz.getSimpleName());
		paramsPanel.addActionListener(this);
		paramsScrollPane.setViewportView(paramsPanel);
		paramsPanel.setSize(paramsScrollPane.getViewport().getViewSize());
		paramsPanel.revalidate();
	}

	private void addInstance(Point2D point){
		instancePanel.addIcon(new PointIcon(point,null));
		instancePanel.repaint();
		setStatus("Instance added at "+point.getX()+", "+point.getY()+" (Total = "+instancePanel.getIcons().size()+").");
	}

	@Override
	public void mouseClicked(MouseEvent e){}

	@Override
	public void mouseEntered(MouseEvent e){}

	@Override
	public void mouseExited(MouseEvent e){}

	@Override
	public void mousePressed(MouseEvent e){
		if(e.getButton()==MouseEvent.BUTTON1){
			if(e.isShiftDown()){
				int points=Integer.parseInt(gaussianField.getText());
				double sigma=Integer.parseInt(gSigmaField.getText());
				double variance=sigma*sigma;
				Gaussian gaussianX=new Gaussian(e.getPoint().x,variance);
				Gaussian gaussianY=new Gaussian(e.getPoint().y,variance);
				for(int i=0;i<points;i++){
					double x=gaussianX.sample(rand);
					double y=gaussianY.sample(rand);
					addInstance(new Point((int)x,(int)y));
				}
			}
			else{
				addInstance(e.getPoint());
			}
		}
	}

	@Override
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

	@Override
	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e){
		if(e.getSource()==clearButton){
			instancePanel.getIcons().clear();
			instancePanel.repaint();
			setStatus("Instances cleared.");
		}
		else if(e.getSource()==randomButton){
			int noise=Integer.parseInt(randomField.getText());
			for(int i=0;i<noise;i++){
				int maxX=instancePanel.getWidth();
				int maxY=instancePanel.getHeight();
				addInstance(new Point(rand.nextInt(maxX),rand.nextInt(maxY)));
			}
		}
		else if(e.getSource()==algorithmChooser){
			displayParametersPanel();
		}
		else if(e.getSource()==simChooser){
			displaySimPanel();
		}
		else{
			run(
					(Class<Clusterer>)algorithmChooser.getSelectedItem(),
					(ParametersPanel)paramsScrollPane.getViewport().getView(),
					(ParametersPanel)simScrollPane.getViewport().getView()
			);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e){
		Object source=e.getItemSelectable();
		if(source==ticCheck){
			for(Component tabComp:tabPane.getComponents()){
				if(tabComp instanceof PointIconPanel&&tabComp!=instancePanel){
					((PointIconPanel)tabComp).setShowYTics(ticCheck.isSelected());
				}
			}
			tabPane.repaint();
		}
	}

	@Override
	public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean hasFocus){
		String text=value.toString();
		if(value instanceof Class){
			text=((Class<?>)value).getSimpleName();
		}
		return defaultListCellRenderer.getListCellRendererComponent(list,text,index,isSelected,hasFocus);
	}

	private void setStatus(String message){
		statusBar.setText(message);
		statusBar.repaint();
	}

	private void writeInstances(OutputStream os){
		PrintWriter writer=IOUtil.getPrintWriter(os,"utf8",true);
		for(PointIcon icon:instancePanel.getIcons()){
			writer.print(icon.getPoint().getX());
			writer.print("\t");
			writer.println(icon.getPoint().getY());
		}
		writer.close();
		setStatus("Wrote instance data");
	}
	
	private void writeInstances(File file){
		try{
			writeInstances(new FileOutputStream(file));
			setStatus("Wrote instance data to "+file.getName());
		}
		catch(Exception e){
			setStatus("Error writing data to "+file.getName());
			e.printStackTrace();
		}
	}

	private void readInstances(InputStream is){
		instancePanel.getIcons().clear();
		try{
			BufferedReader reader=IOUtil.getBufferedReader(is,"utf8");
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
			while(tabPane.getTabCount()>1){
				tabPane.remove(1);
			}
			instancePanel.repaint();
			setStatus("Read instance data");
		}catch(Exception e){
			setStatus("Error reading data");
			e.printStackTrace();
		}
	}

	private void readInstances(File file){
		try{
			readInstances(new FileInputStream(file));
			setStatus("Read instance data from "+file.getName());
		}
		catch(Exception e){
			setStatus("Error reading data from "+file.getName());
			e.printStackTrace();
		}
	}

	private List<String> getToyDataList(String path){
		List<String> list=new ArrayList<String>();
		try{
			File dir=new File(ClassLoader.getSystemResource(path).getFile());
			for(File file:dir.listFiles()){
				if(!file.getName().startsWith(".")){
					list.add(file.getName());
				}
			}
		}
		catch(Exception e){
			log.warn("Cannot load toy data list from: "+path);
		}
		return list;
	}

	public static void main(String[] args){

		JFrame frame=new JFrame(ClusteringPanel.class.getSimpleName());
		frame.add(new ClusteringPanel(),BorderLayout.CENTER);
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}
}
