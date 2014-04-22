package edu.cmu.cs.frank.ml.graph;

import java.io.IOException;
import java.util.Random;

import javax.swing.JFrame;

import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class VizTest{

	public static void main(String[] args)throws IOException{
		JFrame jf=new JFrame();
		VisualizationViewer<Integer,Integer> vv=new VisualizationViewer<Integer,Integer>(new SpringLayout<Integer,Integer>(randomGraph(50,30)));
		jf.getContentPane().add(vv);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
	}

	public static Graph<Integer,Integer> randomGraph(int numVertices,int numEdges){
		
		Random rand=new Random();

		Graph<Integer,Integer> g=new DirectedSparseGraph<Integer,Integer>();
		
		for(int i=0;i<numVertices;i++){
			g.addVertex(i);
		}
		
		for(int i=0;i<numEdges;i++){
			g.addEdge(i,rand.nextInt(numVertices),rand.nextInt(numVertices));
		}
		
		return g;

	}

}
