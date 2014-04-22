package edu.cmu.cs.frank.util;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class HTMLUtil{
	
	public static String extractText(String html){
		StringBuilder buffer=new StringBuilder();
		TextHandler handler=new TextHandler(buffer);
		try{
			SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
			parser.parse(new ByteArrayInputStream(html.getBytes("utf8")),handler);
		}
		catch(Exception e){
			System.err.println("Error parsing HTML:");
			e.printStackTrace();
		}
		return buffer.toString();
	}
	
	private static class TextHandler extends DefaultHandler{
		
		private StringBuilder buffer;
		private boolean inBody;

		public TextHandler(StringBuilder buffer){
			this.buffer=buffer;
			inBody=false;
		}

		@Override
		public void startElement(String uri,String lName,String qName,Attributes attributes)throws SAXException{
			if(qName.equalsIgnoreCase("body")){
				inBody=true;
			}
		}

		@Override
		public void endElement(String uri,String lName,String qName)throws SAXException{
			if(qName.equalsIgnoreCase("body")){
				inBody=false;
			}
		}

		@Override
		public void characters(char[] ch,int start,int length) throws SAXException{
			if(inBody){
				buffer.append(ch,start,length);
			}
		}

	}

}
