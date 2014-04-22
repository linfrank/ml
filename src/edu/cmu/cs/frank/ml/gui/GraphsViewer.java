package edu.cmu.cs.frank.ml.gui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.TestGraphs;

/**
 * Tabbed version of GraphViewer for multiple graphs
 * 
 * @author Frank Lin
 */

public class GraphsViewer<V,E> extends JTabbedPane{

	static final long serialVersionUID=20111018L;
	
	public void addGraph(Graph<V,E> graph,String name){
		add(name,new GraphViewer<V,E>(graph));
	}
	
	@SuppressWarnings("unchecked")
	public Graph<V,E> getGraph(int index){
		return ((GraphViewer<V,E>)getTabComponentAt(index)).getGraph();
	}
	
	public int numGraphs(){
		return getTabCount();
	}
	
	public void removeGraph(int index){
		remove(index);
	}
	
	@SuppressWarnings("unchecked")
	public Graph<V,E> getSelectedGraph(){
		return ((GraphViewer<V,E>)getSelectedComponent()).getGraph();
	}
	
	/**
	 * For testing
	 * 
	 * @param args none
	 */
	
	public static void main(String[] args){
		
		GraphsViewer<String,Number> gv=new GraphsViewer<String,Number>();
		gv.addGraph(TestGraphs.getDemoGraph(),"Demo Graph");
		gv.addGraph(TestGraphs.getOneComponentGraph(),"One Component");
		gv.addGraph(TestGraphs.getSmallGraph(),"Small Graph");
		
    JFrame frame=new JFrame();
    frame.getContentPane().add(gv);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
		
	}

}
