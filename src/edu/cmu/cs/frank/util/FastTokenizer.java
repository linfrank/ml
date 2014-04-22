package edu.cmu.cs.frank.util;


public class FastTokenizer{

	private String text;
	private String delim;
	
	private int start=0;
	
	public FastTokenizer(String text,String delim){
		this.text=text;
		this.delim=delim;
	}
	
	public String next(){
		int dp=nextDelim();
		if(dp<start){
			return null;
		}
		else{
			String ns=text.substring(start,dp);
			start=dp+delim.length();
			return ns;
		}
	}
	
	private boolean isDelim(int pos){
		if(pos+delim.length()>text.length()){
			return false;
		}
		else{
			for(int i=0;i<delim.length();i++){
				if(delim.charAt(i)!=text.charAt(pos+i)){
					return false;
				}
			}
			return true;
		}
	}
	
	private int nextDelim(){
		for(int i=start;i<text.length();i++){
			if(isDelim(i)){
				return i;
			}
		}
		return text.length();
	}
	
	public static void main(String[] args){
		
		String test="I am #$ very tired of #$ stupid things #$ !";
		
		FastTokenizer ft=new FastTokenizer(test," #$ ");
		
		for(String token;(token=ft.next())!=null;System.out.println(token));
		
	}
	
}
