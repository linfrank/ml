package edu.cmu.cs.frank.util;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LinkContext{

	public String targetLink;
	public String before;
	public String during;
	public String after;
	
	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append("Target Link: ").append(targetLink).append("\n");
		b.append("Context Before: ").append(before).append("\n");
		b.append("Context During: ").append(during).append("\n");
		b.append("Context After: ").append(after).append("\n");
		return b.toString();
	}

	public static List<LinkContext> parseLinkContext(String target,String html){
		List<LinkContext> contexts=new LinkedList<LinkContext>();
		ContextHandler handler=new ContextHandler(target,contexts);
		try{
			SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
			parser.parse(new ByteArrayInputStream(html.getBytes("utf8")),handler);
		}
		catch(Exception e){
			System.err.println("Error parsing!");
			e.printStackTrace();
		}
		return contexts;
	}

	private static class ContextHandler extends DefaultHandler{

		private String target;
		private List<LinkContext> contexts;

		private boolean during;

		private StringBuilder buffer;
		private List<Integer> afterOffsets;

		public ContextHandler(String target,List<LinkContext> contexts){
			this.target=target;
			this.contexts=contexts;
			during=false;
		}

		@Override
		public void startElement(String uri,String lName,String qName,Attributes attributes)throws SAXException{
			if(qName.equalsIgnoreCase("body")){
				buffer=new StringBuilder();
				afterOffsets=new LinkedList<Integer>();
			}
			else if(qName.equalsIgnoreCase("a")){
				String url=attributes.getValue("href");
				if(url!=null){
					url=url.toLowerCase();
					if(url.indexOf(target)!=-1){
						LinkContext current=new LinkContext();
						current.targetLink=target;
						current.before=buffer.toString();
						during=true;
						contexts.add(current);
					}
				}
			}
		}

		@Override
		public void endElement(String uri,String lName,String qName)throws SAXException{
			if(qName.equalsIgnoreCase("body")){
				if(contexts.size()==afterOffsets.size()){
					for(int i=0;i<contexts.size();i++){
						contexts.get(i).after=buffer.substring(afterOffsets.get(i));
					}
					for(LinkContext context:contexts){
						context.before=asTokenized(context.before);
						if(context.during!=null){
							context.during=asTokenized(context.during);
						}
						context.after=asTokenized(context.after);
					}
				}
				else{
					System.err.println("Error finding context:");
					System.err.println("Contexts: "+contexts.size());
					System.err.println("After Offsets: "+afterOffsets.size());
				}
			}
			else if(qName.equalsIgnoreCase("a")){
				if(during){
					afterOffsets.add(buffer.length());
					during=false;
				}
			}
		}

		@Override
		public void characters(char[] ch,int start,int length) throws SAXException{
			if(buffer!=null){
				buffer.append(ch,start,length);
			}
			if(during){
				LinkContext current=contexts.get(contexts.size()-1);
				current.during=new String(ch,start,length);
			}
		}
		
		private static String asTokenized(String text){
			List<String> tokens=TextUtil.tokenizeSimple(text.toLowerCase());
			tokens=TextUtil.filterTokensBasic(tokens);
			tokens=TextUtil.filterWhiteSpace(tokens);
			return TextUtil.tokensToString(tokens," ");
		}

	}

}
