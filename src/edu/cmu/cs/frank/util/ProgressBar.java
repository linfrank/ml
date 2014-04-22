package edu.cmu.cs.frank.util;

import javax.swing.JProgressBar;


public class ProgressBar extends JProgressBar{
	
	static final long serialVersionUID=20081119L;

	private int interval;
	
	private Progressable target=null;
	
	public ProgressBar(int interval){
		this.interval=interval;
		setIndeterminate(false);
		setStringPainted(true);
		setString("");
		setMaximum(0);
		setValue(0);
	}
	
	public ProgressBar(){
		this(1000);
	}
	
	public int getInterval(){
		return interval;
	}
	
	public void setInterval(int interval){
		this.interval=interval;
	}
	
	public void track(Progressable target){
		this.target=target;
		new Thread(new ProgressRunner()).start();
	}
	
	public void stopTracking(){
		target=null;
		setIndeterminate(false);
		setStringPainted(true);
		setMaximum(0);
		setValue(0);
	}
	
	private class ProgressRunner implements Runnable{
		@Override
		public void run(){
			while(target!=null){
				Progress progress=target.getProgress();
				if(progress.getDone()<0){
					setIndeterminate(true);
				}
				else{
					setIndeterminate(false);
					setMaximum(1000);
					setValue((int)(progress.getDone()*1000));
				}
				setString(progress.toString());
				try{
					Thread.sleep(interval);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}

}
