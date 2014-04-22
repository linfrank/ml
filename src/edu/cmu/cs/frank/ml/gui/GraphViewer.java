package edu.cmu.cs.frank.ml.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;

/**
 * Panel with graph visualization, navigator panel, and a layout selection
 * 
 * @author Frank Lin
 */

public class GraphViewer<V,E> extends JPanel{

	static final long serialVersionUID=20111018L;
	
	@SuppressWarnings("rawtypes")
	public static final Vector<Class<? extends Layout>> LAYOUTS=new Vector<Class<? extends Layout>>();
	static{
		LAYOUTS.add(FRLayout.class);
		LAYOUTS.add(FRLayout2.class);
		LAYOUTS.add(KKLayout.class);
		LAYOUTS.add(CircleLayout.class);
		LAYOUTS.add(SpringLayout.class);
		LAYOUTS.add(SpringLayout2.class);
		LAYOUTS.add(ISOMLayout.class);
	}
	
	private Graph<V,E> graph;
	private VisualizationViewer<V,E> vv;
	
	public GraphViewer(Graph<V,E> graph){
		
		this.graph=graph;
		
		// initialize viewer
		vv=new VisualizationViewer<V,E>(new FRLayout<V,E>(graph));
		
		// graph mouse
		DefaultModalGraphMouse<V,E> mouse=new DefaultModalGraphMouse<V,E>();
		vv.setGraphMouse(mouse);
		
		// make a combo box for selection mouse modes
		JComboBox modeBox=mouse.getModeComboBox();
		modeBox.addItemListener(mouse.getModeListener());
		modeBox.setSelectedIndex(1);

		// make buttons for zooming in and out
		final ScalingControl scaler=new CrossoverScalingControl();
		JButton plus=new JButton("+");
		plus.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				scaler.scale(vv,1.1f,vv.getCenter());
			}
		});
		JButton minus=new JButton("-");
		minus.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				scaler.scale(vv,1/1.1f,vv.getCenter());
			}
		});

		// make a combo box for selecting layout
		final JComboBox layoutBox=new JComboBox(LAYOUTS);
		layoutBox.setRenderer(new DefaultListCellRenderer(){
			static final long serialVersionUID=20111020L;
			@Override
			public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus){
				String valueString=value.toString();
				valueString=valueString.substring(valueString.lastIndexOf('.')+1);
				return super.getListCellRendererComponent(list,valueString,index,isSelected,cellHasFocus);
			}
		});
		layoutBox.addActionListener(new LayoutChooser(layoutBox,vv));
		layoutBox.setSelectedIndex(0);

		// controls
		JPanel controlPanel=new JPanel();
		controlPanel.add(new JLabel("Layout:"));
		controlPanel.add(layoutBox);
		controlPanel.add(new JLabel("Navigation:"));
		controlPanel.add(modeBox);
		controlPanel.add(plus);
		controlPanel.add(minus);

		// setup the main panel (this object) that includes everything
		this.setLayout(new BorderLayout());
		this.add(vv,BorderLayout.CENTER);
		this.add(controlPanel,BorderLayout.SOUTH);
		
	}
	
	public Graph<V,E> getGraph(){
		return graph;
	}
	
	public VisualizationViewer<V,E> getVV(){
		return vv;
	}


	private class LayoutChooser implements ActionListener{
		
		private final JComboBox jcb;
		private final VisualizationViewer<V,E> vv;
		
		private LayoutChooser(JComboBox jcb,VisualizationViewer<V,E> vv){
			super();
			this.jcb=jcb;
			this.vv=vv;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent arg0){
			Object[] constructorArgs={graph};
			Class<Layout<V,E>> layoutC=(Class<Layout<V,E>>)jcb.getSelectedItem();
			try{
				Constructor<Layout<V,E>> constructor=layoutC.getConstructor(new Class[]{Graph.class});
				Object o=constructor.newInstance(constructorArgs);
				Layout<V,E> l=(Layout<V,E>)o;
				vv.setGraphLayout(l);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * For testing
	 * 
	 * @param args none
	 */
	
	public static void main(String[] args){
		
    JFrame frame=new JFrame();
    frame.getContentPane().add(new GraphViewer<String,Number>(TestGraphs.getDemoGraph()));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
		
	}

}
