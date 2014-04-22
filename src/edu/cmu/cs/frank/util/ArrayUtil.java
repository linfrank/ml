package edu.cmu.cs.frank.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.cs.frank.util.MapUtil.MapComparator;


public class ArrayUtil{ 

	public static int[] copy(int[] a){
		return Arrays.copyOf(a,a.length);
	}
	
	public static int[][] copy(int[][] a){
		int[][] copy=new int[a.length][];
		for(int i=0;i<a.length;i++){
			copy[i]=copy(a[i]);
		}
		return copy;
	}
	
	public static double[] copy(double[] a){
		return Arrays.copyOf(a,a.length);
	}

	public static String[] copy(String[] a){
		String[] copy=new String[a.length];
		for(int i=0;i<a.length;i++){
			copy[i]=a[i];
		}
		return copy;
	}

	public static String[][] copy(String[][] a){
		String[][] copy=new String[a.length][];
		for(int i=0;i<a.length;i++){
			copy[i]=copy(a[i]);
		}
		return copy;
	}

	public static void reverse(int[] a){
		int temp;
		for(int i=0;i<a.length/2;i++){
			int j=a.length-i-1;
			temp=a[i];
			a[i]=a[j];
			a[j]=temp;
		}
	}

	public static void reverse(double[] a){
		double temp;
		for(int i=0;i<a.length/2;i++){
			int j=a.length-i-1;
			temp=a[i];
			a[i]=a[j];
			a[j]=temp;
		}
	}
	
	public static void reverse(byte[] a){
		byte temp;
		for(int i=0;i<a.length/2;i++){
			int j=a.length-i-1;
			temp=a[i];
			a[i]=a[j];
			a[j]=temp;
		}
	}

	public static int sum(int[] a){
		int sum=0;
		for(int i=0;i<a.length;i++){
			sum+=a[i];
		}
		return sum;
	}

	public static int sum(int[][] a){
		int sum=0;
		for(int i=0;i<a.length;i++){
			sum+=sum(a[i]);
		}
		return sum;
	}

	public static int sumRow(int[][] a,int row){
		return sum(a[row]);
	}

	public static int sumColumn(int[][] a,int column){
		int sum=0;
		for(int i=0;i<a.length;i++){
			sum+=a[i][column];
		}
		return sum;
	}

	public static int[] add(int[] a,int[] b){
		int[] sum=new int[a.length];
		for(int i=0;i<a.length;i++){
			sum[i]=a[i]+b[i];
		}
		return sum;
	}

	public static double sum(double[] a){
		double sum=0.0;
		for(int i=0;i<a.length;i++){
			sum+=a[i];
		}
		return sum;
	}

	public static double sum(double[][] a){
		double sum=0.0;
		for(int i=0;i<a.length;i++){
			sum+=sum(a[i]);
		}
		return sum;
	}

	public static double sumRow(double[][] a,int row){
		return sum(a[row]);
	}

	public static double sumColumn(double[][] a,int column){
		int sum=0;
		for(int i=0;i<a.length;i++){
			sum+=a[i][column];
		}
		return sum;
	}

	public static double[] add(double[] a,double[] b){
		double[] sum=new double[a.length];
		for(int i=0;i<a.length;i++){
			sum[i]=a[i]+b[i];
		}
		return sum;
	}

	public static double[] multiply(double[] a,double b){
		double[] product=new double[a.length];
		for(int i=0;i<a.length;i++){
			product[i]=a[i]*b;
		}
		return product;
	}

	public static double[] divide(double[] a,double b){
		double[] product=new double[a.length];
		for(int i=0;i<a.length;i++){
			product[i]=a[i]/b;
		}
		return product;
	}

	public static double mean(double[] a){
		return sum(a)/a.length;
	}

	public static double average(double[] a){
		return sum(a)/a.length;
	}
	
	public static double stdDev(double[] a,double mean){
		double devSum=0.0;
		for(int i=0;i<a.length;i++){
			double diff=a[i]-mean;
			devSum+=diff*diff;
		}
		return Math.sqrt(devSum/a.length);
	}

	public static double stdDev(double[] a){
		return stdDev(a,mean(a));
	}

	public static void normalize(double[] a){
		double sum=sum(a);
		for(int i=0;i<a.length;i++){
			a[i]/=sum;
		}
	}
	
	public static void normalizeStat(double[] a,double mean,double stdDev){
		if(stdDev!=0.0){
			for(int i=0;i<a.length;i++){
				a[i]=(a[i]-mean)/stdDev;
			}
		}
	}

	public static void normalizeStat(double[] a){
		double mean=mean(a);
		normalizeStat(a,mean,stdDev(a,mean));
	}

	public static void normalize(double[][] a){
		double sum=sum(a);
		for(int i=0;i<a.length;i++){
			for(int j=0;j<a[i].length;j++){
				a[i][j]/=sum;
			}
		}
	}

	public static int maxIndex(double[] a){
		if(a.length<1){
			return -1;
		}
		else{
			int max=0;
			for(int i=1;i<a.length;i++){
				if(a[i]>a[max]){
					max=i;
				}
			}
			return max;
		}
	}

	public static double max(double[] a){
		return a[maxIndex(a)];
	}

	public static int minIndex(double[] a){
		if(a.length<1){
			return -1;
		}
		else{
			int min=0;
			for(int i=1;i<a.length;i++){
				if(a[i]<a[min]){
					min=i;
				}
			}
			return min;
		}
	}

	public static double min(double[] a){
		return a[minIndex(a)];
	}

	public static int maxIndex(int[] a){
		if(a.length<1){
			return -1;
		}
		else{
			int max=0;
			for(int i=1;i<a.length;i++){
				if(a[i]>a[max]){
					max=i;
				}
			}
			return max;
		}
	}

	public static int max(int[] a){
		return a[maxIndex(a)];
	}
	
	public static int minIndex(int[] a){
		if(a.length<1){
			return -1;
		}
		else{
			int min=0;
			for(int i=1;i<a.length;i++){
				if(a[i]<a[min]){
					min=i;
				}
			}
			return min;
		}
	}

	public static int min(int[] a){
		return a[minIndex(a)];
	}

	public static double mode(double[] a){
		double[] copy=copy(a);
		Arrays.sort(copy);
		return copy[copy.length/2];
	}

	public static int modeIndex(double[] a){
		double mode=mode(a);
		for(int i=0;i<a.length;i++){
			if(a[i]==mode){
				return i;
			}
		}
		return -1;
	}

	public static String toString(int[][] a){
		StringBuffer b=new StringBuffer();
		for(int i=0;i<a.length;i++){
			b.append(Arrays.toString(a[i])).append("\n");
		}
		return b.toString();
	}

	public static String toString(double[][] a){
		StringBuffer b=new StringBuffer();
		for(int i=0;i<a.length;i++){
			b.append(Arrays.toString(a[i])).append("\n");
		}
		return b.toString();
	}

	public static void fill(int[][] a,int value){
		for(int i=0;i<a.length;i++){
			Arrays.fill(a[i],value);
		}
	}

	public static void fill(double[][] a,double value){
		for(int i=0;i<a.length;i++){
			Arrays.fill(a[i],value);
		}
	}

	public static boolean and(boolean[] a){
		for(int i=0;i<a.length;i++){
			if(!a[i]){
				return false;
			}
		}
		return true;
	}

	public static boolean or(boolean[] a){
		for(int i=0;i<a.length;i++){
			if(a[i]){
				return true;
			}
		}
		return false;
	}

	public static double[] sample(double[] original,int sampleSize){

		if(sampleSize>=original.length){
			return copy(original);
		}

		double[] sample=new double[sampleSize];
		double step=(double)original.length/sampleSize;
		double sampler=0;

		for(int i=0;i<sample.length;i++){
			sample[i]=original[(int)sampler];
			sampler+=step;
		}

		return sample; 

	}

	public static String toTable(String[][] a){
		int maxLength=0;
		for(int i=0;i<a.length;i++){
			for(int j=0;j<a[i].length;j++){
				int length=a[i][j].length();
				if(length>maxLength){
					maxLength=length;
				}
			}
		}
		StringBuilder b=new StringBuilder();
		for(int i=0;i<a.length;i++){
			for(int j=0;j<a[i].length;j++){
				b.append(" ").append(TextUtil.padLeft(a[i][j],' ',maxLength));
			}
			b.append("\n");
		}
		return b.toString();
	}

	public static String toTable(int[][] a){
		String[][] s=new String[a.length][];
		for(int i=0;i<a.length;i++){
			s[i]=new String[a[i].length];
			for(int j=0;j<a[i].length;j++){
				s[i][j]=String.valueOf(a[i][j]);
			}
		}
		return toTable(s);
	}

	public static <T> List<T> toList(T[] a){
		List<T> list=new ArrayList<T>(a.length);
		for(int i=0;i<a.length;i++){
			list.add(a[i]);
		}
		return list;
	}

	public static boolean[] toBooleanArray(List<Boolean> list){
		boolean[] a=new boolean[list.size()];
		for(int i=0;i<list.size();i++){
			a[i]=list.get(i);
		}
		return a;
	}

	public static int[] toIntArray(List<Integer> list){
		int[] a=new int[list.size()];
		for(int i=0;i<list.size();i++){
			a[i]=list.get(i);
		}
		return a;
	}

	public static double[] toDoubleArray(List<Double> list){
		double[] a=new double[list.size()];
		for(int i=0;i<list.size();i++){
			a[i]=list.get(i);
		}
		return a;
	}
	
	public static int[] toIntArray(Integer[] a){
		int[] b=new int[a.length];
		for(int i=0;i<a.length;i++){
			b[i]=a[i];
		}
		return b;
	}
	
	public static double[] toDoubleArray(Double[] a){
		double[] b=new double[a.length];
		for(int i=0;i<a.length;i++){
			b[i]=a[i];
		}
		return b;
	}
	
	public static Integer[] toIntegerArray(int[] a){
		Integer[] b=new Integer[a.length];
		for(int i=0;i<a.length;i++){
			b[i]=a[i];
		}
		return b;
	}
	
	public static Double[] toDoubleArray(double[] a){
		Double[] b=new Double[a.length];
		for(int i=0;i<a.length;i++){
			b[i]=a[i];
		}
		return b;
	}
	
	public static <T extends Comparable<T>> int[] sortIndex(T[] a){
		Map<Integer,T> map=new HashMap<Integer,T>(a.length);
		Integer[] indices=new Integer[a.length];
		for(int i=0;i<a.length;i++){
			map.put(i,a[i]);
			indices[i]=i;
		}
		Arrays.sort(indices,new MapComparator<Integer,T>(map));
		return toIntArray(indices);
	}
	
	public static int[] sortIndex(int[] a){
		return sortIndex(toIntegerArray(a));
	}
	
	public static int[] sortIndex(double[] a){
		return sortIndex(toDoubleArray(a));
	}

}
