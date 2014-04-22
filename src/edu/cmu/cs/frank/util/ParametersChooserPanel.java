package edu.cmu.cs.frank.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;


public class ParametersChooserPanel extends JPanel implements ListCellRenderer,ActionListener{
	
	static final long serialVersionUID=20091117L;
	
	public static final Dimension ITEM_PREF=new Dimension(120,30);
	public static final Dimension ITEM_MAX=new Dimension(120,30);
	public static final Dimension ITEM_MIN=new Dimension(50,30);
	
	public static final Dimension BUTTON_PREF=new Dimension(120,30);
	public static final Dimension BUTTON_MAX=new Dimension(120,30);
	public static final Dimension BUTTON_MIN=new Dimension(80,30);
	
	private JComboBox chooser;
	private JScrollPane scrollPane;
	private JButton setButton;
	
	private Parametizable parametized;
	
	public ParametersChooserPanel(List<Class<? extends Parametizable>> classes,String title,int selected){
		
		if(title!=null){
			TitledBorder tb=BorderFactory.createTitledBorder(title);
			tb.setTitleJustification(TitledBorder.CENTER);
			this.setBorder(tb);
		}

		chooser=new JComboBox(new Vector<Class<? extends Parametizable>>(classes));
		chooser.setSelectedIndex(selected);
		chooser.setPreferredSize(ITEM_PREF);
		chooser.setMaximumSize(ITEM_MAX);
		chooser.setMinimumSize(ITEM_MIN);
		chooser.setAlignmentX(CENTER_ALIGNMENT);
		chooser.setRenderer(this);
		chooser.addActionListener(this);
		
		scrollPane=new JScrollPane();
		displayParametersPanel();
		
		setButton=new JButton("Set");
		setButton.setPreferredSize(BUTTON_PREF);
		setButton.setMaximumSize(BUTTON_MAX);
		setButton.setMinimumSize(BUTTON_MIN);
		setButton.setAlignmentX(CENTER_ALIGNMENT);
		setButton.addActionListener(this); 
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		add(chooser);
		add(scrollPane);
		add(setButton);
		
	}
	
	public Parametizable getParametized(){
		if(parametized==null){
			instantiate();
		}
		return parametized;
	}
	
	private void displayParametersPanel(){
		ParametersPanel paramsPanel=((ParametersPanel)scrollPane.getViewport().getView());
		if(paramsPanel!=null){
			paramsPanel.removeActionListener(this);
		}
		@SuppressWarnings("unchecked")
		Class<Parametizable> clazz=(Class<Parametizable>)chooser.getSelectedItem();
		paramsPanel=new ParametersPanel(Parameters.getDefault(clazz));
		paramsPanel.addActionListener(this);
		scrollPane.setViewportView(paramsPanel);
		paramsPanel.setSize(scrollPane.getViewport().getViewSize());
		paramsPanel.revalidate();
	}
	
	private void instantiate(){

		@SuppressWarnings("unchecked")
		Class<Parametizable> clazz=(Class<Parametizable>)chooser.getSelectedItem();
		ParametersPanel paramsPanel=(ParametersPanel)scrollPane.getViewport().getView();

		if(paramsPanel==null||!paramsPanel.parseParameters()){
			parametized=null;
		}
		else{
			parametized=Parameters.newInstance(clazz,paramsPanel.getParameters());
		}

	}
	
	private List<ActionListener> actionListeners=new ArrayList<ActionListener>();
	
	public void addActionListener(ActionListener al){
		actionListeners.add(al);
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		if(e.getSource()==setButton){
			instantiate();
			setButton.setForeground(Color.BLACK);
			setButton.repaint();
			ActionEvent ae=new ActionEvent(this,0,"set");
			for(ActionListener al:actionListeners){
				al.actionPerformed(ae);
			}
		}
		else if(e.getSource()==chooser){
			displayParametersPanel();
			setButton.setForeground(Color.RED);
			setButton.repaint();
		}
	}
	
	private ListCellRenderer defaultListCellRenderer=new DefaultListCellRenderer();
	
	@Override
	public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean hasFocus){
		String text=((Class<?>)value).getSimpleName();
		return defaultListCellRenderer.getListCellRendererComponent(list,text,index,isSelected,hasFocus);
	}
	

}
