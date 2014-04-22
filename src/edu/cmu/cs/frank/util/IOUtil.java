package edu.cmu.cs.frank.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class IOUtil{

	public static boolean fileExists(String fileName){
		return (new File(fileName)).exists();
	}

	public static BufferedReader getBufferedReader(File file,String encoding){
		try{
			return new BufferedReader(new InputStreamReader(new FileInputStream(file),encoding));
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static BufferedReader getBufferedReader(String fileName,String encoding){
		return getBufferedReader(new File(fileName),encoding);
	}

	public static BufferedReader getBufferedReader(InputStream is,String encoding){
		try{
			return new BufferedReader(new InputStreamReader(is,encoding));
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static BufferedReader getBufferedReader(File file){
		return getBufferedReader(file,System.getProperty("file.encoding"));
	}

	public static BufferedReader getBufferedReader(String fileName){
		return getBufferedReader(new File(fileName));
	}

	public static BufferedReader getBufferedReader(InputStream is){
		return getBufferedReader(is,System.getProperty("file.encoding"));
	}

	public static BufferedReader getBufferedReaderByExtension(File file,String encoding){

		String ext="";
		if(file.getName().lastIndexOf('.')>0){
			ext=file.getName().substring(file.getName().lastIndexOf('.'));
		}

		try{
			if(ext.equalsIgnoreCase(".gz")||ext.equalsIgnoreCase(".gzip")){
				//System.err.println("Reading "+file.getName()+" as a GZIP stream.");
				return getBufferedReader(new GZIPInputStream(new FileInputStream(file)),encoding);
			}
			else if(ext.equalsIgnoreCase(".z")||ext.equalsIgnoreCase(".zip")){
				//System.err.println("Reading "+file.getName()+" as a Zip stream.");
				return getBufferedReader(new ZipInputStream(new FileInputStream(file)),encoding);
			}
			else{
				return getBufferedReader(file,encoding);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
		
	}
	
	public static BufferedReader getBufferedReaderByExtension(String fileName,String encoding){
		return getBufferedReaderByExtension(new File(fileName),encoding);
	}

	public static PrintWriter getPrintWriter(File file,String encoding,boolean autoflush){
		try{
			return new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),encoding),autoflush);
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static PrintWriter getPrintWriter(OutputStream os,String encoding,boolean autoflush){
		try{
			return new PrintWriter(new OutputStreamWriter(os,encoding),autoflush);
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static PrintWriter getPrintWriter(String fileName,String encoding,boolean autoflush){
		return getPrintWriter(new File(fileName),encoding,autoflush);
	}

	public static PrintWriter getPrintWriter(File file){
		return getPrintWriter(file,System.getProperty("file.encoding"),true);
	}

	public static PrintWriter getPrintWriter(String fileName){
		return getPrintWriter(new File(fileName));
	}

	public static PrintWriter getPrintWriter(OutputStream os){
		return getPrintWriter(os,System.getProperty("file.encoding"),true);
	}

	public static String readFile(File file,String encoding)throws IOException{
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(file),encoding));
		StringBuffer buffer=new StringBuffer();
		for(String nextLine;(nextLine=reader.readLine())!=null;buffer.append(nextLine+"\n"));
		reader.close();
		return buffer.toString();
	}

	public static String readFile(File file)throws IOException{
		return readFile(file,System.getProperty("file.encoding"));
	}

	public static String readFile(String fileName,String encoding)throws IOException{
		return readFile(new File(fileName),encoding);
	}

	public static String readFile(String fileName)throws IOException{
		return readFile(fileName,System.getProperty("file.encoding"));
	}

	public static List<String> readLines(File file,String encoding)throws IOException{
		List<String> lines=new ArrayList<String>();
		BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(file),encoding));
		for(String nextLine;(nextLine=reader.readLine())!=null;lines.add(nextLine));
		reader.close();
		return lines;
	}

	public static List<String> readLines(String fileName,String encoding)throws IOException{
		return readLines(new File(fileName),encoding);
	}

	public static List<String> readLines(String fileName)throws IOException{
		return readLines(fileName,System.getProperty("file.encoding"));
	}

	public static Object readObject(File file)throws IOException,ClassNotFoundException{
		ObjectInputStream input=new ObjectInputStream(new FileInputStream(file));
		Object o=input.readObject();
		input.close();
		return o;
	}

	public static Object readObject(String fileName)throws IOException,ClassNotFoundException{
		return readObject(new File(fileName));
	}

	public static void writeFile(File file,String content,String encoding)throws IOException{
		BufferedReader reader=new BufferedReader(new StringReader(content));
		PrintWriter writer=new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),encoding),true);
		for(String nextLine;(nextLine=reader.readLine())!=null;writer.println(nextLine));
		reader.close();
		writer.close();
	}

	public static void writeFile(String fileName,String content,String encoding)throws IOException{
		writeFile(new File(fileName),content,encoding);
	}

	public static void writeFile(File file,String content)throws IOException{
		writeFile(file,content,System.getProperty("file.encoding"));
	}

	public static void writeFile(String fileName,String content)throws IOException{
		writeFile(new File(fileName),content);
	}

	public static void writeFile(File file)throws IOException{
		writeFile(file,"");
	}

	public static void writeFile(String fileName)throws IOException{
		writeFile(new File(fileName));
	}

	public static void writeLines(String fileName,List<String> lines,String encoding)throws IOException{
		PrintWriter writer=new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName),encoding),true);
		for(String line:lines){
			writer.println(line);
		}
		writer.close();
	}

	public static void writeLines(String fileName,List<String> lines)throws IOException{
		writeLines(fileName,lines,System.getProperty("file.encoding"));
	}

	public static void writeObject(File file,Object o)throws IOException{
		ObjectOutputStream output=new ObjectOutputStream(new FileOutputStream(file));
		output.writeObject(o);
		output.close();
	}

	public static void writeObject(String fileName,Object o)throws IOException{
		writeObject(new File(fileName),o);
	}

}
