package edu.cmu.cs.frank.gui;

import javax.swing.JTextArea;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 
 * @author Frank Lin
 */

public class JTextAreaAppender extends AppenderSkeleton{

	private JTextArea textArea;
	
	public JTextAreaAppender(JTextArea textArea){
		this.textArea=textArea;
	}
	
	public JTextAreaAppender(){
		this(null);
	}
	
	public JTextArea getTextArea(){
		return textArea;
	}
	
	public void setTextArea(JTextArea textArea){
		this.textArea=textArea;
	}

	@Override
	protected void append(LoggingEvent event){
		textArea.append(event.getLevel().toString());
		textArea.append(" - ");
		textArea.append(event.getMessage().toString());
		textArea.append("\n");
	}

	@Override
	public void close(){
		textArea=null;
	}

	@Override
	public boolean requiresLayout(){
		return false;
	}

}
