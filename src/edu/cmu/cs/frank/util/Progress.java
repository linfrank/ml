package edu.cmu.cs.frank.util;

/**
 * A container class for indication of progress, mainly for implementing a progress bar
 */

public class Progress{

	private String status;
	private double done;
	private Progressable delegate;

	public Progress(String status,double done){
		this.status=status;
		this.done=done;
	}

	public Progress(String status){
		this(status,-1);
	}
	
	public Progress(double done){
		this(null,done);
	}
	
	public Progress(){
		this(null,-1);
	}

	public String getStatus(){
		if(delegate==null){
			return status;
		}
		else{
			return delegate.getProgress().getStatus();
		}
	}

	public void setStatus(String status){
		this.status=status;
	}

	public double getDone(){
		if(delegate==null){
			return done;
		}
		else{
			return delegate.getProgress().getDone();
		}
	}

	public void setDone(double done){
		this.done=done;
	}

	public void setDelegate(Progressable delegate){
		this.delegate=delegate;
	}
	
	public void removeDelegate(){
		this.delegate=null;
	}

	public void update(String status,double done){
		setStatus(status);
		setDone(done);
		removeDelegate();
	}
	
	public void update(String status){
		setStatus(status);
		removeDelegate();
	}
	
	public void update(double done){
		setDone(done);
		removeDelegate();
	}
	
	public void update(Progressable delegate){
		setDelegate(delegate);
	}

	@Override
	public String toString(){
		if(delegate!=null){
			return delegate.getProgress().toString();
		}
		else if(status!=null&&done>=0){
			return status.concat(" (").concat(FormatUtil.d1(done*100)).concat("%)");
		}
		else if(status==null&&done>=0){
			return FormatUtil.d1(done*100).concat("%");
		}
		else if(status!=null&&done<0){
			return status;
		}
		else{
			return "";
		}
	}

}
