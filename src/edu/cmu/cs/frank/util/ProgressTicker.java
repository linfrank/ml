package edu.cmu.cs.frank.util;

import java.io.IOException;
import java.io.Writer;


public class ProgressTicker{

	private static final String[] spinner=new String[]{"\\","|","/","-"};

	private int interval=0;
	private int spin=0;

	private Writer writer=null;
	private Progressable target=null;

	public ProgressTicker(Writer writer,int interval){
		this.writer=writer;
		this.interval=interval;
	}

	public ProgressTicker(Writer writer){
		this(writer,1000);
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
	}

	private class ProgressRunner implements Runnable{
		@Override
		public void run(){
			while(target!=null){
				try{
					Progress progress=target.getProgress();
					writer.append("\r");
					if(progress.getStatus()!=null){
						writer.append(progress.getStatus());
					}
					writer.append(" [");
					if(progress.getDone()<0){
						writer.append(spinner[spin]);
						spin=(spin+1)%spinner.length;
					}
					else{
						int statusLength=progress.getStatus()==null?0:progress.getStatus().length();
						int width=80-statusLength-3;
						int ticks=(int)(progress.getDone()*width);
						for(int i=0;i<ticks;i++){
							writer.append("\u2592");
						}
						writer.append(spinner[spin]);
						spin=(spin+1)%spinner.length;
						for(int i=ticks+1;i<width;i++){
							writer.append(" ");
						}
					}
					writer.append("]");
					writer.flush();
					try{
						Thread.sleep(interval);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}
	
}
