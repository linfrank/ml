/*
 * Frank Lin
 * 
 */

package edu.cmu.cs.frank.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class NetUtil{

	private static String defaultEncoding="utf-8";
	private static int defaultTimeout=30000;
	private static String defaultUseragent="Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1) Gecko/20061010 Firefox/2.0";

	public static InputStream getUrlInputStream(String urlString,String encoding,int timeout,String useragent){
		try{
			URLConnection connection=(new URL(urlString)).openConnection();
			//what is the difference of these timeouts?
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestProperty("User-Agent",useragent);
			connection.setRequestProperty("Accept-Charset",encoding);
			connection.connect();
			InputStream is=connection.getInputStream();
			return is;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static InputStream getUrlInputStream(String urlString,String encoding){
		return getUrlInputStream(urlString,encoding,defaultTimeout,defaultUseragent);
	}

	public static InputStream getUrlInputStream(String urlString){
		return getUrlInputStream(urlString,defaultEncoding);
	}

	public static String getUrlContent(String urlString,String encoding,int timeout,String useragent){
		InputStream is=getUrlInputStream(urlString,encoding,timeout,useragent);
		try{
			BufferedReader reader=new BufferedReader(new InputStreamReader(is,encoding));
			StringBuilder builder=new StringBuilder();
			for(String nextLine;(nextLine=reader.readLine())!=null;builder.append(nextLine).append("\n"));
			reader.close();
			return builder.toString();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getUrlContent(String urlString){
		return getUrlContent(urlString,defaultEncoding,defaultTimeout,defaultUseragent);
	}
	
	public static String normalizeBlogUrl(String url){
		int start;
		if(url.startsWith("http://www.")){
			start=11;
		}
		else if(url.startsWith("http://")){
			start=7;
		}
		else if(url.startsWith("www.")){
			start=4;
		}
		else{
			start=0;
		}
		int end;
		if(url.endsWith("/")){
			end=url.length()-1;
		}
		else{
			end=url.length();
		}
		return url.substring(start,end).toLowerCase();
	}

}
