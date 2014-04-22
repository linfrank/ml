package edu.cmu.cs.frank.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


public class BasicFrame extends JFrame{
	
	static final long serialVersionUID=20080807L;
	
	protected JTextField inputField;
	protected JTextArea outputArea;
	protected JTextField statusBar;
	
	public BasicFrame(String title){
		
		super(title);
		
		inputField=new JTextField();
		
		outputArea=new JTextArea();
		outputArea.setEditable(false);
		JScrollPane scrollPane=new JScrollPane(outputArea);
		
		statusBar=new JTextField();
		statusBar.setEditable(false);
		
		setLayout(new BorderLayout());
		add(inputField,BorderLayout.NORTH);
		add(scrollPane,BorderLayout.CENTER);
		add(statusBar,BorderLayout.SOUTH);
		
		this.setSize(800,600);
		this.setLocationRelativeTo(null);
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
	}
	
	public BasicFrame(){
		this("");
	}
	
	public String getInput(){
		return inputField.getText();
	}
	
	public void addInputListener(ActionListener l){
		inputField.addActionListener(l);
	}
	
	public void setOutput(String s){
		outputArea.setText(s);
		outputArea.setCaretPosition(s.length());
	}
	
	public void appendOutput(String s){
		outputArea.append(s);
		outputArea.setCaretPosition(outputArea.getText().length());
	}
	
	public void setStatus(String s){
		statusBar.setText(s);
	}

}
