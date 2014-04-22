package edu.cmu.cs.frank.util;

import java.util.Collection;


public interface Tree<T>{
	
	public boolean isRoot();
	
	public boolean isLeaf();
	
	public Tree<T> getRoot();
	
	public Tree<T> getParent();
	
	public Tree<T> getChild(int i);
	
	public boolean addChild(Tree<T> child);
	
	public boolean removeChild(Tree<T> child);
	
	public int numChildren();
	
	public Collection<? extends Tree<T>> children();
	
	public int depth();
	
	public int height();
	
	public int numLeaves();
	
	public T getData();
	
	public void setData(T data);

}
