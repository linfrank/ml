package edu.cmu.cs.frank.x;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.cmu.cs.frank.util.IOUtil;
import edu.cmu.cs.frank.util.ScoreTable;

/**
 * Creates an author disambiguation datasets from DBLP dump
 * 
 * @author frank
 */

public class DBLPData{

	public static final Pattern AMB_NAME_PAT=Pattern.compile("(.+) (\\d\\d\\d\\d)$");
	
	public static final Set<String> PUBS=new HashSet<String>();
	static{
		PUBS.add("article");
		PUBS.add("inproceedings");
	}

	public static void main(String[] args)throws Exception{

		if(args.length!=3){
			System.out.println("Usage: <option> <input file> <output dir>");
			System.out.println(" option=stat|full|abb1|abb2|nomi");
			return;
		}

		String option=args[0];
		String input=args[1];
		String output=args[2];

		System.out.println("Parsing XML...");

		SAXParser parser=SAXParserFactory.newInstance().newSAXParser();

		if(option.equals("stat")){

			StatHandler handler=new StatHandler();
			parser.parse(new File(input),handler);

			System.out.println("Unique auth: "+handler.authCounts.size());
			System.out.println("Unique full: "+handler.fullCounts.size());
			System.out.println("Unique abb1: "+handler.abb1Counts.size());
			System.out.println("Unique abb2: "+handler.abb2Counts.size());

			System.out.println("Writing counts to files...");
			writeCounts(handler.authCounts,output,"auth");
			writeCounts(handler.fullCounts,output,"full");
			writeCounts(handler.abb1Counts,output,"abb1");
			writeCounts(handler.abb2Counts,output,"abb2");

		}
		else{

			DataHandler handler=new DataHandler(option,new FileOutputStream(output+"/flat-"+option+".txt"));
			parser.parse(new File(input),handler);

		}

	}

	private static class DataHandler extends DefaultHandler{

		private String type;
		private PrintWriter writer;

		private String article=null;
		private boolean inAuthor=false;
		private StringBuilder author=new StringBuilder();

		public DataHandler(String type,OutputStream output) throws IOException{
			super();
			this.type=type;
			writer=new PrintWriter(new OutputStreamWriter(output,"utf8"));
		}

		@Override
		public void startElement(String uri,String localName,String qName,Attributes att)throws SAXException{
			if(PUBS.contains(qName)){
				article=att.getValue("key");
			}
			else if(qName.equals("author")){
				inAuthor=true;
			}
		}

		@Override
		public void endElement(String uri,String localName,String qName)throws SAXException{
			if(PUBS.contains(qName)){
				article=null;
			}
			else if(qName.equals("author")){
				
				String auth=author.toString().trim();
				
				if(article!=null&&auth.length()>0){

					String name=auth;
					Matcher m=AMB_NAME_PAT.matcher(auth);
					if(m.matches()){
						name=m.group(1);
					}

					if(type.equals("abb1")){
						name=abbreviate(name,false);
					}
					else if(type.equals("abb2")){
						name=abbreviate(name,true);
					}
					else if(type.equals("nomi")){
						name=removeMiddleInitials(name);
					}

					writer.print(auth.replace(' ','_'));
					writer.print('\t');
					writer.print(name.replace(' ','_'));
					writer.print('\t');
					writer.println(article);
					
				}

				author.setLength(0);
				inAuthor=false;

			}
		}

		@Override
		public void endDocument() throws SAXException{
			writer.close();
		}

		@Override
		public void characters(char[] text,int start,int length) throws SAXException{
			if(inAuthor){
				author.append(text,start,length);
			}
		}

	}

	private static class StatHandler extends DefaultHandler{

		public int totalArticles=0;

		public ScoreTable<String> authCounts=new ScoreTable<String>();
		public ScoreTable<String> fullCounts=new ScoreTable<String>();
		public ScoreTable<String> abb1Counts=new ScoreTable<String>();
		public ScoreTable<String> abb2Counts=new ScoreTable<String>();

		//private String article=null;
		private boolean inAuthor=false;
		private StringBuilder author=new StringBuilder();

		@Override
		public void startElement(String uri,String localName,String qName,Attributes att)throws SAXException{
			if(PUBS.contains(qName)){
				//article=att.getValue("key");
			}
			else if(qName.equals("author")){
				inAuthor=true;
			}
		}

		@Override
		public void endElement(String uri,String localName,String qName)throws SAXException{
			if(PUBS.contains(qName)){
				totalArticles++;
			}
			if(qName.equals("author")){

				inAuthor=false;

				String name=author.toString().trim();
				authCounts.increment(name);

				Matcher m=AMB_NAME_PAT.matcher(name);
				if(m.matches()){
					name=m.group(1);
				}

				fullCounts.increment(name);
				abb1Counts.increment(abbreviate(name,false));
				abb2Counts.increment(abbreviate(name,true));

				author.setLength(0);

			}
		}

		@Override
		public void characters(char[] text,int start,int length) throws SAXException{
			if(inAuthor){
				author.append(text,start,length);
			}
		}

	}

	private static void writeCounts(ScoreTable<String> counts,String outDir,String name){
		PrintWriter w1=IOUtil.getPrintWriter(outDir+"/"+name+"_names.txt");
		PrintWriter w2=IOUtil.getPrintWriter(outDir+"/"+name+"_counts.txt");
		for(String key:counts.sortedKeysDescend()){
			w1.println(key);
			w2.println(counts.get(key));
		}
		w1.close();
		w2.close();
	}

	private static String abbreviate(String full,boolean ignoreMiddle){
		StringBuilder b=new StringBuilder();
		String[] tokens=full.split("\\s+");
		int l=getLastNameIndex(tokens);
		// write caps
		b.append(tokens[0].charAt(0));
		if(!ignoreMiddle){
			for(int i=1;i<l;i++){
				b.append(" ").append(tokens[i].charAt(0));
			}
		}
		// write last name
		for(int i=l;i<tokens.length;i++){
			b.append(" ").append(tokens[i]);
		}
		return b.toString();
	}
	
	private static String removeMiddleInitials(String full){
		StringBuilder b=new StringBuilder();
		String[] tokens=full.split("\\s+");
		int l=getLastNameIndex(tokens);
		// write first name
		b.append(tokens[0]);
		// write last name
		for(int i=l;i<tokens.length;i++){
			b.append(" ").append(tokens[i]);
		}
		return b.toString();
	}

	private static int getLastNameIndex(String[] tokens){
		int i=tokens.length-1;
		while(i-1>0&&Character.isLowerCase(tokens[i-1].charAt(0))){i--;}
		return i;
	}

}
