package edu.cmu.cs.frank.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class BinaryTree<T> implements Tree<T>{

	private BinaryTree<T> parent;

	private BinaryTree<T> left;

	private BinaryTree<T> right;

	private T data;

	public BinaryTree(T data){
		this.data=data;
	}

	public BinaryTree(){
		this(null);
	}

	@Override
	public boolean isRoot(){
		return parent==null;
	}

	@Override
	public boolean isLeaf(){
		return left==null&&right==null;
	}

	@Override
	public Tree<T> getRoot(){
		return parent==null?this:parent.getRoot();
	}

	@Override
	public Tree<T> getParent(){
		return parent;
	}

	@Override
	public Tree<T> getChild(int i){
		if(i==0){
			return left;
		}
		else if(i==1){
			return right;
		}
		else{
			return null;
		}
	}

	@Override
	public boolean addChild(Tree<T> child){
		if(child instanceof BinaryTree){
			if(left==null){
				left=(BinaryTree<T>)child;
				left.parent=this;
				return true;
			}
			else if(right==null){
				right=(BinaryTree<T>)child;
				right.parent=this;
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	@Override
	public boolean removeChild(Tree<T> child){
		if(left==child){
			left.parent=null;
			left=null;
			return true;
		}
		if(right==child){
			right.parent=null;
			right=null;
			return true;
		}
		return false;
	}

	public BinaryTree<T> getLeft(){
		return left;
	}

	public BinaryTree<T> getRight(){
		return right;
	}

	public void setLeft(BinaryTree<T> left){
		left.parent=this;
		this.left=left;
	}

	public void setRight(BinaryTree<T> right){
		right.parent=this;
		this.right=right;
	}

	@Override
	public Collection<Tree<T>> children(){
		List<Tree<T>> children=new ArrayList<Tree<T>>(2);
		if(left!=null){
			children.add(left);
		}
		if(right!=null){
			children.add(right);
		}
		return children;
	}

	@Override
	public int depth(){
		if(parent==null){
			return 0;
		}
		else{
			return parent.depth()+1;
		}
	}

	@Override
	public int height(){
		if(left!=null&&right!=null){
			return Math.max(left.height(),right.height())+1;
		}
		else if(left!=null){
			return left.height()+1;
		}
		else if(right!=null){
			return right.height()+1;
		}
		else{
			return 0;
		}
	}

	@Override
	public T getData(){
		return data;
	}

	@Override
	public void setData(T data){
		this.data=data;
	}

	@Override
	public int numChildren(){
		int num=0;
		if(left!=null){
			num++;
		}
		if(right!=null){
			num++;
		}
		return num;
	}

	@Override
	public int numLeaves(){
		if(isLeaf()){
			return 1;
		}
		else{
			int num=0;
			if(left!=null){
				num+=left.numLeaves();
			}
			if(right!=null){
				num+=right.numLeaves();
			}
			return num;
		}
	}

	private String toString(int level){
		StringBuilder b=new StringBuilder();
		b.append(TextUtil.padLeft("",' ',level)).append("Data: ").append(data).append("\n");
		if(left!=null){
			b.append(TextUtil.padLeft("",' ',level)).append("Left:").append("\n");
			b.append(left.toString(level+1));
		}
		if(right!=null){
			b.append(TextUtil.padLeft("",' ',level)).append("Right:").append("\n");
			b.append(right.toString(level+1));
		}
		return b.toString();
	}

	@Override
	public String toString(){
		return toString(0);
	}

}
