package edu.cmu.cs.frank.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class RandomUtil{
	
	private static final Random random=new Random();
	
	public static int[] pair(int n,Random rand){
		int[] pair=new int[2];
		// pick first item
		pair[0]=rand.nextInt(n);
		// pick second item
		pair[1]=rand.nextInt(n-1);
		if(pair[0]==pair[1]){
			pair[1]=n-1;
		}
		return pair;
	}
	
	public static int[] pair(int n){
		return pair(n,random);
	}
	
	public static int[] permute(int n,Random rand){
		// initialize an ordered list
		int[] permutation=new int[n];
		for(int i=0;i<n;i++){
			permutation[i]=i;
		}
		// draw without replacement one by one
		for(int i=n;i>0;i--){
			// draw an index
			int drawn=rand.nextInt(i);
			// swap values
			int temp=permutation[drawn]; 
			permutation[drawn]=permutation[i-1];
			permutation[i-1]=temp;
		}
		return permutation;
	}
	
	public static int[] permute(int n){
		return permute(n,random);
	}
	
	public static <T> List<T> draw(List<T> list,int numToDraw,Random rand){
		List<T> shuffled=new ArrayList<T>(list);
		Collections.shuffle(shuffled,rand);
		List<T> drawn=new ArrayList<T>(numToDraw);
		for(int i=0;i<numToDraw;i++){
			drawn.add(shuffled.get(i));
		}
		return drawn;
	}
	
	public static <T> List<T> draw(List<T> list,int numToDraw){
		return draw(list,numToDraw,random);
	}
	
	public static List<Integer> drawInteger(int max,int numToDraw){
		List<Integer> list=new ArrayList<Integer>(max);
		for(int i=0;i<max;i++){
			list.add(i);
		}
		return draw(list,numToDraw);
	}
	
	public static int drawWithDistribution(double[] distribution,Random rand){
		
		double total=0.0;
		for(int i=0;i<distribution.length;i++){
			assert distribution[i]>0;
			total+=distribution[i];
		}
		
		double point=rand.nextDouble()*total;
		
		double running=0.0;
		for(int i=0;i<distribution.length;i++){
			running+=distribution[i];
			if(point<running){
				return i;
			}
		}
		return distribution.length-1;
		
	}
	
	// Produce an int from 1 (inclusive) to items (inclusive), observing power law
	public static int getPowerLawInt(int items,double power,Random rand){
		double diviser=rand.nextDouble()*(items-1)*power+1.0;
		return (int)(items/diviser);
	}
	
	public static void main(String[] args){
		
		System.out.println(Arrays.toString(permute(10)));
		
	}

}
