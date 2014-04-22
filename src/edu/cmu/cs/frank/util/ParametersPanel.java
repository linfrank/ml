package edu.cmu.cs.frank.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.cmu.cs.frank.util.Parameters.Choice;
import edu.cmu.cs.frank.util.Parameters.ParameterException;

public class ParametersPanel extends JPanel implements ActionListener{

	static final long serialVersionUID=20080512L;

	public static final Dimension ITEM_PREF=new Dimension(100,30);
	public static final Dimension ITEM_MAX=new Dimension(150,30);
	public static final Dimension ITEM_MIN=new Dimension(50,30);

	public static final Border DEFAULT_BORDER=new EmptyBorder(2,2,2,2);

	private Parameters p;
	private Map<Object,String> compMap;
	private List<ActionListener> actionListeners;

	public ParametersPanel(Parameters p,String title){

		this.p=p;
		compMap=new HashMap<Object,String>();
		actionListeners=new ArrayList<ActionListener>();

		if(title!=null){
			TitledBorder tb=BorderFactory.createTitledBorder(title);
			tb.setTitleJustification(TitledBorder.CENTER);
			this.setBorder(tb);
		}

		if(p!=null){
			Field[] fields=p.getFields();
			for(int i=0;i<fields.length;i++){
				String name=fields[i].getName();
				if(isDescendantOf(fields[i].getType(),Parameters.class)){
					Parameters childParams=null;
					try{
						childParams=(Parameters)fields[i].getType().newInstance();
					}catch(Exception e){
						e.printStackTrace();
					}
					ParametersPanel childPanel=new ParametersPanel(childParams,p.getPrintName(name));
					compMap.put(childPanel,name);
					this.add(childPanel);
				}
				else if(fields[i].getType().equals(Choice.class)){
					Object fieldVal=null;
					try{
						fieldVal=fields[i].get(p);
					}catch(IllegalAccessException iae){
						iae.printStackTrace();
					}
					Choice choice=(Parameters.Choice)fieldVal;
					ChoicePanel choicePanel=new ChoicePanel(choice,name);
					compMap.put(choicePanel,name);
					this.add(choicePanel);
				}
				else{
					JLabel label=new JLabel(p.getPrintName(name)+":");
					label.setAlignmentX(CENTER_ALIGNMENT);
					this.add(label);
					if(fields[i].getType().isEnum()){
						Object[] constants=fields[i].getType().getEnumConstants();
						String[] constantNames=new String[constants.length];
						for(int j=0;j<constants.length;j++){
							Enum<?> constant=(Enum<?>)constants[j];
							constantNames[j]=constant.name();
						}
						JComboBox chooser=new JComboBox(constantNames);
						chooser.setPreferredSize(ITEM_PREF);
						chooser.setMaximumSize(ITEM_MAX);
						chooser.setMinimumSize(ITEM_MIN);
						chooser.setAlignmentX(CENTER_ALIGNMENT);
						compMap.put(chooser,name);
						this.add(chooser);
					}
					else if(fields[i].getType().equals(Boolean.TYPE)){
						JRadioButton trueButton=new JRadioButton(String.valueOf(true));
						trueButton.setActionCommand(String.valueOf(true));
						JRadioButton falseButton=new JRadioButton(String.valueOf(false));
						falseButton.setActionCommand(String.valueOf(false));
						ButtonGroup group=new ButtonGroup();
						group.add(trueButton);
						group.add(falseButton);
						if(p.getValue(name).equals(Boolean.TRUE)){
							trueButton.setSelected(true);
						}
						else{
							falseButton.setSelected(true);
						}
						compMap.put(group,name);
						JPanel booleanPane=new JPanel();
						booleanPane.add(trueButton);
						booleanPane.add(falseButton);
						booleanPane.setPreferredSize(ITEM_PREF);
						booleanPane.setMaximumSize(ITEM_MAX);
						booleanPane.setMinimumSize(ITEM_MIN);
						booleanPane.setAlignmentX(CENTER_ALIGNMENT);
						this.add(booleanPane);
					}
					else if(fields[i].getType().isArray()){
						// create the gui component
						JTextField field=new JTextField();
						field.setPreferredSize(ITEM_PREF);
						field.setMaximumSize(ITEM_MAX);
						field.setMinimumSize(ITEM_MIN);
						field.addActionListener(this);
						field.setAlignmentX(CENTER_ALIGNMENT);
						compMap.put(field,name);
						this.add(field);
						// fill it
						Object array=p.getValue(name);
						int length=Array.getLength(array);
						StringBuilder b=new StringBuilder();
						for(int j=0;j<length;j++){
							if(j!=0){
								b.append(", ");
							}
							b.append(Array.get(array,j));
						}
						field.setText(b.toString());
					}
					else{
						// create the gui component
						JTextField field=new JTextField();
						field.setPreferredSize(ITEM_PREF);
						field.setMaximumSize(ITEM_MAX);
						field.setMinimumSize(ITEM_MIN);
						field.addActionListener(this);
						field.setAlignmentX(CENTER_ALIGNMENT);
						compMap.put(field,name);
						this.add(field);
						// fill it
						Object value=p.getValue(name);
						String s=value==null?"":String.valueOf(value);
						field.setText(s);
					}
				}
			}
		}

		this.add(new Box.Filler(new Dimension(180,0),new Dimension(180,0),new Dimension(180,Integer.MAX_VALUE)));

		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

	}

	public ParametersPanel(Parameters p){
		this(p,null);
	}

	public void addActionListener(ActionListener actionListener){
		actionListeners.add(actionListener);
	}

	public void removeActionListener(ActionListener actionListener){
		actionListeners.remove(actionListener);
	}

	public boolean parseParameters(){
		for(Map.Entry<Object,String> entry:compMap.entrySet()){
			Object comp=entry.getKey();
			String name=entry.getValue();
			if(comp instanceof ParametersPanel){
				ParametersPanel child=(ParametersPanel)comp;
				child.parseParameters();
				p.setValue(name,child.getParameters());
			}
			if(comp instanceof ChoicePanel){
				ChoicePanel choice=(ChoicePanel)comp;
				ParametersPanel child=choice.getChosenPanel();
				child.parseParameters();
				((Parameters.Choice)p.getValue(name)).changeChoice(choice.getChosenIndex(),child.getParameters());
			}
			else{
				String value=null;
				if(comp instanceof JComboBox){
					JComboBox chooser=(JComboBox)comp;
					value=chooser.getSelectedItem().toString();
				}
				else if(comp instanceof JTextField){
					JTextField field=(JTextField)comp;
					value=field.getText();
				}
				else if(comp instanceof ButtonGroup){
					ButtonGroup group=(ButtonGroup)comp;
					value=group.getSelection().getActionCommand();
				}
				if(value!=null){
					try{
						p.parse(name,value);
					}catch(ParameterException pe){
						JOptionPane.showMessageDialog(this.getRootPane(),pe.getMessage());
						return false;
					}
				}
			}
		}
		return true;
	}

	public Parameters getParameters(){
		return p;
	}

	private class ChoicePanel extends JPanel implements ActionListener{

		static final long serialVersionUID=20090210L;

		private Parameters.Choice choice;
		private JComboBox chooser;

		public ChoicePanel(Parameters.Choice choice,String name){

			this.choice=choice;

			if(name!=null){
				TitledBorder tb=BorderFactory.createTitledBorder(name);
				tb.setTitleJustification(TitledBorder.CENTER);
				this.setBorder(tb);
			}

			if(choice==null){
				chooser=new JComboBox();
			}
			else{
				chooser=new JComboBox(choice.getChoices().toArray());
			}

			chooser.setRenderer(new DefaultListCellRenderer(){
				static final long serialVersionUID=20090210L;
				@Override
				public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean hasFocus){
					String text=((Parameters)value).getEnclosingClass().getSimpleName();
					return super.getListCellRendererComponent(list,text,index,isSelected,hasFocus);
				}
			});
			chooser.setPreferredSize(ITEM_PREF);
			chooser.setMaximumSize(ITEM_MAX);
			chooser.setMinimumSize(ITEM_MIN);
			chooser.setAlignmentX(CENTER_ALIGNMENT);

			setLayout(new BorderLayout());
			add(chooser,BorderLayout.NORTH);
			add(new ParametersPanel(choice.getChosen()),BorderLayout.CENTER);
			chooser.addActionListener(this);
			compMap.put(this,name);

		}

		@Override
		public void actionPerformed(ActionEvent e){
			choice.setChosen(chooser.getSelectedIndex());
			remove(1);
			add(new ParametersPanel(choice.getChosen()),BorderLayout.CENTER);
			getRootPane().validate();
		}

		public int getChosenIndex(){
			return chooser.getSelectedIndex();
		}

		public ParametersPanel getChosenPanel(){
			return (ParametersPanel)getComponent(1);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e){
		parseParameters();
		for(ActionListener listener:actionListeners){
			listener.actionPerformed(e);
		}
	}

	private static boolean isDescendantOf(Class<?> d,Class<?> a){
		Class<?> sup=d.getSuperclass();
		if(sup==null){
			return false;
		}
		else if(sup.equals(a)){
			return true;
		}
		else{
			return isDescendantOf(sup,a);
		}
	}

}
