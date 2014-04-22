/**
 * TextUtil.java
 * 
 * @author frank
 */

package edu.cmu.cs.frank.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class TextUtil{

	static Logger log=Logger.getLogger(TextUtil.class);

	private static Pattern XML_ESCAPE_PAT=Pattern.compile("&(\\w+?);");
	private static Pattern XML_UNICODE_ESCAPE_PAT=Pattern.compile("&#(\\d+?);");
	private static Map<String,String> XML_SYMBOL_ESCAPE_MAP=new HashMap<String,String>();
	static{
		// non-standard ones
		XML_SYMBOL_ESCAPE_MAP.put("euro","€");
		XML_SYMBOL_ESCAPE_MAP.put("apos","'");
		// ASCII symbols
		XML_SYMBOL_ESCAPE_MAP.put("quot",""+(char)34);
		XML_SYMBOL_ESCAPE_MAP.put("amp",""+(char)38);
		XML_SYMBOL_ESCAPE_MAP.put("lt",""+(char)60);
		XML_SYMBOL_ESCAPE_MAP.put("gt",""+(char)62);
		XML_SYMBOL_ESCAPE_MAP.put("nbsp",""+(char)160);
		XML_SYMBOL_ESCAPE_MAP.put("iexcl",""+(char)161);
		XML_SYMBOL_ESCAPE_MAP.put("cent",""+(char)162);
		XML_SYMBOL_ESCAPE_MAP.put("pound",""+(char)163);
		XML_SYMBOL_ESCAPE_MAP.put("curren",""+(char)164);
		XML_SYMBOL_ESCAPE_MAP.put("yen",""+(char)165);
		XML_SYMBOL_ESCAPE_MAP.put("brvbar",""+(char)166);
		XML_SYMBOL_ESCAPE_MAP.put("sect",""+(char)167);
		XML_SYMBOL_ESCAPE_MAP.put("uml",""+(char)168);
		XML_SYMBOL_ESCAPE_MAP.put("copy",""+(char)169);
		XML_SYMBOL_ESCAPE_MAP.put("ordf",""+(char)170);
		XML_SYMBOL_ESCAPE_MAP.put("laquo",""+(char)171);
		XML_SYMBOL_ESCAPE_MAP.put("not",""+(char)172);
		XML_SYMBOL_ESCAPE_MAP.put("shy",""+(char)173);
		XML_SYMBOL_ESCAPE_MAP.put("reg",""+(char)174);
		XML_SYMBOL_ESCAPE_MAP.put("macr",""+(char)175);
		XML_SYMBOL_ESCAPE_MAP.put("deg",""+(char)176);
		XML_SYMBOL_ESCAPE_MAP.put("plusmn",""+(char)177);
		XML_SYMBOL_ESCAPE_MAP.put("sup2",""+(char)178);
		XML_SYMBOL_ESCAPE_MAP.put("sup3",""+(char)179);
		XML_SYMBOL_ESCAPE_MAP.put("acute",""+(char)180);
		XML_SYMBOL_ESCAPE_MAP.put("micro",""+(char)181);
		XML_SYMBOL_ESCAPE_MAP.put("para",""+(char)182);
		XML_SYMBOL_ESCAPE_MAP.put("middot",""+(char)183);
		XML_SYMBOL_ESCAPE_MAP.put("cedil",""+(char)184);
		XML_SYMBOL_ESCAPE_MAP.put("sup1",""+(char)185);
		XML_SYMBOL_ESCAPE_MAP.put("ordm",""+(char)186);
		XML_SYMBOL_ESCAPE_MAP.put("raquo",""+(char)187);
		XML_SYMBOL_ESCAPE_MAP.put("frac14",""+(char)188);
		XML_SYMBOL_ESCAPE_MAP.put("frac12",""+(char)189);
		XML_SYMBOL_ESCAPE_MAP.put("frac34",""+(char)190);
		XML_SYMBOL_ESCAPE_MAP.put("iquest",""+(char)191);
		XML_SYMBOL_ESCAPE_MAP.put("Agrave",""+(char)192);
		XML_SYMBOL_ESCAPE_MAP.put("Aacute",""+(char)193);
		XML_SYMBOL_ESCAPE_MAP.put("Acirc",""+(char)195);
		XML_SYMBOL_ESCAPE_MAP.put("Atilde",""+(char)195);
		XML_SYMBOL_ESCAPE_MAP.put("Auml",""+(char)196);
		XML_SYMBOL_ESCAPE_MAP.put("Aring",""+(char)197);
		XML_SYMBOL_ESCAPE_MAP.put("AElig",""+(char)198);
		XML_SYMBOL_ESCAPE_MAP.put("Ccedil",""+(char)199);
		XML_SYMBOL_ESCAPE_MAP.put("Egrave",""+(char)200);
		XML_SYMBOL_ESCAPE_MAP.put("Eacute",""+(char)201);
		XML_SYMBOL_ESCAPE_MAP.put("Ecric",""+(char)202);
		XML_SYMBOL_ESCAPE_MAP.put("Euml",""+(char)203);
		XML_SYMBOL_ESCAPE_MAP.put("Igrave",""+(char)204);
		XML_SYMBOL_ESCAPE_MAP.put("Iacute",""+(char)205);
		XML_SYMBOL_ESCAPE_MAP.put("Icirc",""+(char)206);
		XML_SYMBOL_ESCAPE_MAP.put("Iuml",""+(char)207);
		XML_SYMBOL_ESCAPE_MAP.put("ETH",""+(char)208);
		XML_SYMBOL_ESCAPE_MAP.put("Ntilde",""+(char)209);
		XML_SYMBOL_ESCAPE_MAP.put("Ograve",""+(char)210);
		XML_SYMBOL_ESCAPE_MAP.put("Oacute",""+(char)211);
		XML_SYMBOL_ESCAPE_MAP.put("Ocirc",""+(char)212);
		XML_SYMBOL_ESCAPE_MAP.put("Otilde",""+(char)213);
		XML_SYMBOL_ESCAPE_MAP.put("Ouml",""+(char)214);
		XML_SYMBOL_ESCAPE_MAP.put("times",""+(char)215);
		XML_SYMBOL_ESCAPE_MAP.put("Oslash",""+(char)216);
		XML_SYMBOL_ESCAPE_MAP.put("Ugrave",""+(char)217);
		XML_SYMBOL_ESCAPE_MAP.put("Uacute",""+(char)218);
		XML_SYMBOL_ESCAPE_MAP.put("Ucirc",""+(char)219);
		XML_SYMBOL_ESCAPE_MAP.put("Uuml",""+(char)220);
		XML_SYMBOL_ESCAPE_MAP.put("Yacute",""+(char)221);
		XML_SYMBOL_ESCAPE_MAP.put("THORN",""+(char)222);
		XML_SYMBOL_ESCAPE_MAP.put("szlig",""+(char)223);
		XML_SYMBOL_ESCAPE_MAP.put("agrave",""+(char)224);
		XML_SYMBOL_ESCAPE_MAP.put("aacute",""+(char)225);
		XML_SYMBOL_ESCAPE_MAP.put("acirc",""+(char)226);
		XML_SYMBOL_ESCAPE_MAP.put("atilde",""+(char)227);
		XML_SYMBOL_ESCAPE_MAP.put("auml",""+(char)228);
		XML_SYMBOL_ESCAPE_MAP.put("aring",""+(char)229);
		XML_SYMBOL_ESCAPE_MAP.put("aelig",""+(char)230);
		XML_SYMBOL_ESCAPE_MAP.put("ccedil",""+(char)231);
		XML_SYMBOL_ESCAPE_MAP.put("egrave",""+(char)232);
		XML_SYMBOL_ESCAPE_MAP.put("eacute",""+(char)233);
		XML_SYMBOL_ESCAPE_MAP.put("ecirc",""+(char)234);
		XML_SYMBOL_ESCAPE_MAP.put("euml",""+(char)235);
		XML_SYMBOL_ESCAPE_MAP.put("igrave",""+(char)236);
		XML_SYMBOL_ESCAPE_MAP.put("iacute",""+(char)237);
		XML_SYMBOL_ESCAPE_MAP.put("icirc",""+(char)238);
		XML_SYMBOL_ESCAPE_MAP.put("iuml",""+(char)239);
		XML_SYMBOL_ESCAPE_MAP.put("eth",""+(char)240);
		XML_SYMBOL_ESCAPE_MAP.put("ntilde",""+(char)241);
		XML_SYMBOL_ESCAPE_MAP.put("ograve",""+(char)242);
		XML_SYMBOL_ESCAPE_MAP.put("oacute",""+(char)243);
		XML_SYMBOL_ESCAPE_MAP.put("ocirc",""+(char)244);
		XML_SYMBOL_ESCAPE_MAP.put("otilde",""+(char)245);
		XML_SYMBOL_ESCAPE_MAP.put("ouml",""+(char)246);
		XML_SYMBOL_ESCAPE_MAP.put("divide",""+(char)247);
		XML_SYMBOL_ESCAPE_MAP.put("oslash",""+(char)248);
		XML_SYMBOL_ESCAPE_MAP.put("ugrave",""+(char)249);
		XML_SYMBOL_ESCAPE_MAP.put("uacute",""+(char)250);
		XML_SYMBOL_ESCAPE_MAP.put("ucirc",""+(char)251);
		XML_SYMBOL_ESCAPE_MAP.put("uuml",""+(char)252);
		XML_SYMBOL_ESCAPE_MAP.put("yacute",""+(char)253);
		XML_SYMBOL_ESCAPE_MAP.put("thorn",""+(char)254);
		// ae
		XML_SYMBOL_ESCAPE_MAP.put("oelig",""+(char)339);
		// greek upper
		XML_SYMBOL_ESCAPE_MAP.put("Alpha",""+(char)913);
		XML_SYMBOL_ESCAPE_MAP.put("Beta",""+(char)914);
		XML_SYMBOL_ESCAPE_MAP.put("Gamma",""+(char)915);
		XML_SYMBOL_ESCAPE_MAP.put("Delta",""+(char)916);
		XML_SYMBOL_ESCAPE_MAP.put("Epsilon",""+(char)917);
		XML_SYMBOL_ESCAPE_MAP.put("Zeta",""+(char)918);
		XML_SYMBOL_ESCAPE_MAP.put("Eta",""+(char)919);
		XML_SYMBOL_ESCAPE_MAP.put("Theta",""+(char)920);
		XML_SYMBOL_ESCAPE_MAP.put("Iota",""+(char)921);
		XML_SYMBOL_ESCAPE_MAP.put("Kappa",""+(char)922);
		XML_SYMBOL_ESCAPE_MAP.put("Lambda",""+(char)923);
		XML_SYMBOL_ESCAPE_MAP.put("Mu",""+(char)924);
		XML_SYMBOL_ESCAPE_MAP.put("Nu",""+(char)925);
		XML_SYMBOL_ESCAPE_MAP.put("Xi",""+(char)926);
		XML_SYMBOL_ESCAPE_MAP.put("Omicron",""+(char)927);
		XML_SYMBOL_ESCAPE_MAP.put("Pi",""+(char)928);
		XML_SYMBOL_ESCAPE_MAP.put("Rho",""+(char)929);
		// no final upper sigma
		XML_SYMBOL_ESCAPE_MAP.put("Sigma",""+(char)931);
		XML_SYMBOL_ESCAPE_MAP.put("Tau",""+(char)932);
		XML_SYMBOL_ESCAPE_MAP.put("Upsilon",""+(char)933);
		XML_SYMBOL_ESCAPE_MAP.put("Phi",""+(char)934);
		XML_SYMBOL_ESCAPE_MAP.put("Chi",""+(char)935);
		XML_SYMBOL_ESCAPE_MAP.put("Psi",""+(char)936);
		XML_SYMBOL_ESCAPE_MAP.put("Omega",""+(char)937);
		// greak lower
		XML_SYMBOL_ESCAPE_MAP.put("alpha",""+(char)945);
		XML_SYMBOL_ESCAPE_MAP.put("beta",""+(char)946);
		XML_SYMBOL_ESCAPE_MAP.put("gamma",""+(char)947);
		XML_SYMBOL_ESCAPE_MAP.put("delta",""+(char)948);
		XML_SYMBOL_ESCAPE_MAP.put("epsilon",""+(char)949);
		XML_SYMBOL_ESCAPE_MAP.put("zeta",""+(char)950);
		XML_SYMBOL_ESCAPE_MAP.put("eta",""+(char)951);
		XML_SYMBOL_ESCAPE_MAP.put("theta",""+(char)952);
		XML_SYMBOL_ESCAPE_MAP.put("iota",""+(char)953);
		XML_SYMBOL_ESCAPE_MAP.put("kappa",""+(char)954);
		XML_SYMBOL_ESCAPE_MAP.put("lambda",""+(char)955);
		XML_SYMBOL_ESCAPE_MAP.put("mu",""+(char)956);
		XML_SYMBOL_ESCAPE_MAP.put("nu",""+(char)957);
		XML_SYMBOL_ESCAPE_MAP.put("xi",""+(char)958);
		XML_SYMBOL_ESCAPE_MAP.put("omicron",""+(char)959);
		XML_SYMBOL_ESCAPE_MAP.put("pi",""+(char)960);
		XML_SYMBOL_ESCAPE_MAP.put("rho",""+(char)961);
		XML_SYMBOL_ESCAPE_MAP.put("sigmaf",""+(char)962);
		XML_SYMBOL_ESCAPE_MAP.put("sigma",""+(char)963);
		XML_SYMBOL_ESCAPE_MAP.put("tau",""+(char)964);
		XML_SYMBOL_ESCAPE_MAP.put("upsilon",""+(char)965);
		XML_SYMBOL_ESCAPE_MAP.put("phi",""+(char)966);
		XML_SYMBOL_ESCAPE_MAP.put("chi",""+(char)967);
		XML_SYMBOL_ESCAPE_MAP.put("psi",""+(char)968);
		XML_SYMBOL_ESCAPE_MAP.put("omega",""+(char)969);
		// punctuations
		XML_SYMBOL_ESCAPE_MAP.put("thinsp",""+(char)8201);
		XML_SYMBOL_ESCAPE_MAP.put("ndash",""+(char)8211);
		XML_SYMBOL_ESCAPE_MAP.put("mdash",""+(char)8212);
		XML_SYMBOL_ESCAPE_MAP.put("lsquo",""+(char)8216);
		XML_SYMBOL_ESCAPE_MAP.put("rsquo",""+(char)8217);
		XML_SYMBOL_ESCAPE_MAP.put("sbquo",""+(char)8218);
		XML_SYMBOL_ESCAPE_MAP.put("ldquo",""+(char)8220);
		XML_SYMBOL_ESCAPE_MAP.put("rdquo",""+(char)8221);
		XML_SYMBOL_ESCAPE_MAP.put("prime",""+(char)8242);
		// textual symbols
		XML_SYMBOL_ESCAPE_MAP.put("dagger",""+(char)8224);
		XML_SYMBOL_ESCAPE_MAP.put("bull",""+(char)8226);
		XML_SYMBOL_ESCAPE_MAP.put("hellip",""+(char)8230);
		XML_SYMBOL_ESCAPE_MAP.put("lsaquo",""+(char)8249);
		XML_SYMBOL_ESCAPE_MAP.put("rsaquo",""+(char)8250);
		XML_SYMBOL_ESCAPE_MAP.put("trade",""+(char)8482);
		XML_SYMBOL_ESCAPE_MAP.put("alefsym",""+(char)8501);
		XML_SYMBOL_ESCAPE_MAP.put("larr",""+(char)8592);
		XML_SYMBOL_ESCAPE_MAP.put("uarr",""+(char)8593);
		XML_SYMBOL_ESCAPE_MAP.put("rarr",""+(char)8594);
		XML_SYMBOL_ESCAPE_MAP.put("darr",""+(char)8595);
		XML_SYMBOL_ESCAPE_MAP.put("harr",""+(char)8596);
		// math symbols
		XML_SYMBOL_ESCAPE_MAP.put("frasl",""+(char)8260);
		XML_SYMBOL_ESCAPE_MAP.put("forall",""+(char)8704);
		XML_SYMBOL_ESCAPE_MAP.put("minus",""+(char)8722);
		XML_SYMBOL_ESCAPE_MAP.put("lowast",""+(char)8727);
		XML_SYMBOL_ESCAPE_MAP.put("infin",""+(char)8734);
		XML_SYMBOL_ESCAPE_MAP.put("sim",""+(char)8764);
		XML_SYMBOL_ESCAPE_MAP.put("ne",""+(char)8800);
		XML_SYMBOL_ESCAPE_MAP.put("otimes",""+(char)8855);
		// bracketing symbols
		XML_SYMBOL_ESCAPE_MAP.put("lceil",""+(char)8968);
		XML_SYMBOL_ESCAPE_MAP.put("rceil",""+(char)8969);
		XML_SYMBOL_ESCAPE_MAP.put("lfloor",""+(char)8970);
		XML_SYMBOL_ESCAPE_MAP.put("rfloor",""+(char)8971);
		// play card suits
		XML_SYMBOL_ESCAPE_MAP.put("spades",""+(char)9824);
		XML_SYMBOL_ESCAPE_MAP.put("clubs",""+(char)9827);
		XML_SYMBOL_ESCAPE_MAP.put("hearts",""+(char)9829);
		XML_SYMBOL_ESCAPE_MAP.put("diams",""+(char)9830);
	}

	public static String decodeXmlUnicodeEscapes(String s){
		StringBuffer b=new StringBuffer();
		Matcher m=XML_UNICODE_ESCAPE_PAT.matcher(s);
		while(m.find()){
			String uChar=String.valueOf((char)Integer.parseInt(m.group(1)));
			m.appendReplacement(b,Matcher.quoteReplacement(uChar));
		}
		m.appendTail(b);
		return b.toString();
	}

	public static String decodeXmlSymbolEscapes(String s){
		StringBuffer b=new StringBuffer();
		Matcher m=XML_ESCAPE_PAT.matcher(s);
		while(m.find()){
			String symbol=XML_SYMBOL_ESCAPE_MAP.get(m.group(1));
			if(symbol!=null){
				m.appendReplacement(b,Matcher.quoteReplacement(symbol));
			}
			else{
				symbol=XML_SYMBOL_ESCAPE_MAP.get(m.group(1).toLowerCase());
				if(symbol!=null){
					m.appendReplacement(b,Matcher.quoteReplacement(symbol));
				}
			}
		}
		m.appendTail(b);
		return b.toString();
	}

	public static String decodeXmlEscapes(String s){
		StringBuffer b=new StringBuffer();
		Matcher m=XML_ESCAPE_PAT.matcher(s);
		while(m.find()){
			String found=m.group(1);
			String replacement=null;
			if(found.startsWith("#")&&isInteger(found,1,found.length())){
				replacement=String.valueOf((char)Integer.parseInt(found));
			}
			else{
				replacement=XML_SYMBOL_ESCAPE_MAP.get(found);
				if(replacement==null){
					replacement=XML_SYMBOL_ESCAPE_MAP.get(found.toLowerCase());
					if(replacement==null){
						log.warn("Cannot decode XML escape sequence: "+m.group(0));
						replacement=found;
					}
				}
			}
			m.appendReplacement(b,Matcher.quoteReplacement(replacement));
		}
		m.appendTail(b);
		return b.toString();
	}

	public static boolean isInteger(String s,int start,int end){
		for(int i=start;i<end;i++){
			if(!Character.isDigit(s.charAt(i))){
				return false;
			}
		}
		return true;
	}

	public static boolean isInteger(String s){
		return isInteger(s,0,s.length());
	}

	public static String normalizeSpaces(String s){
		s=s.replaceAll("\\s+"," ");
		s=s.replaceAll("[\\r\\n]+","\n");
		return s;
	}

	private static final Pattern OPEN_SEARCH_TERM_TAG=Pattern.compile("<(([Bb])|([Ff][Oo][Nn][Tt] .+?))>");
	private static final Pattern CLOSE_SEARCH_TERM_TAG=Pattern.compile("</(([Bb])|([Ff][Oo][Nn][Tt]))>");

	public static String replaceSearchTermTags(String s){
		Matcher m=OPEN_SEARCH_TERM_TAG.matcher(s);
		s=m.replaceAll("[[[");
		m=CLOSE_SEARCH_TERM_TAG.matcher(s);
		s=m.replaceAll("]]]");
		return s;
	}

	private static final Pattern XML_TAG_PAT=Pattern.compile("<.+?>",Pattern.DOTALL);

	public static String stripXmlTags(String s,boolean space){
		Matcher m=XML_TAG_PAT.matcher(s);
		if(space){
			return m.replaceAll(" ");
		}
		else{
			return m.replaceAll("");
		}
	}

	public static String stripXmlTags(String s){
		return stripXmlTags(s,false);
	}
	
	public static String stripQuotes(String s){
		if(s.length()>1){
			int start=0;
			int end=s.length();
			if(s.charAt(start)=='"'){
				start++;
			}
			if(s.charAt(end-1)=='"'){
				end--;
			}
			if(start!=0||end!=s.length()){
				return s.substring(start,end);
			}
			else{
				return s;
			}
		}
		else{
			return s;
		}
	}

	private static final Pattern LINK_PAT=Pattern.compile("href\\=\"(.+?)\"");

	public static List<String> collectLinks(String s){
		List<String> links=new ArrayList<String>();
		Matcher m=LINK_PAT.matcher(s);
		while(m.find()){
			links.add(m.group(1));
		}
		return links;
	}

	public static String aggregate(List<String> strings){
		if(strings.size()>0){
			StringBuilder b=new StringBuilder();
			for(String string:strings){
				b.append(string).append("\n");
			}
			return b.toString();
		}
		else{
			return "";
		}
	}

	public static List<String> tokenizeSimple(String s){
		List<String> tokens=new ArrayList<String>();
		if(s.length()>0){
			//create token holder
			StringBuilder token=new StringBuilder();
			//process the first character
			int prev=s.codePointAt(0);
			int prevType=Character.getType(prev);
			Character.UnicodeBlock prevBlock=Character.UnicodeBlock.of(prev);
			token.appendCodePoint(prev);
			for(int i=1;i<s.length();i++){
				int curr=s.codePointAt(i);
				int currType=Character.getType(curr);
				Character.UnicodeBlock currBlock=Character.UnicodeBlock.of(curr);
				boolean delim;
				if(currType!=prevType||currBlock!=prevBlock){
					delim=true;
				}
				else{
					delim=false;
				}
				if(delim){
					String cand=token.toString().trim();
					if(cand.length()>0){
						tokens.add(cand);
					}
					token=new StringBuilder();
					token.appendCodePoint(curr);
				}
				else{
					token.appendCodePoint(curr);
				}
				prev=curr;
				prevType=currType;
				prevBlock=currBlock;
			}
			//process the characters in token holder
			tokens.add(token.toString());
		}
		return tokens;
	}

	private static final Pattern BASIC_TOKEN_FILTER=Pattern.compile("["+
			"\\p{Lu}"+
			"\\p{Mn}"+
			"\\p{Mc}"+
			"\\p{Me}"+
			"\\p{Pc}"+
			"\\p{Pd}"+
			"\\p{Ps}"+
			"\\p{Pe}"+
//			"\\p{Pi}"+
			"“<«"+
//			"\\p{Pf}"+
			"”>»"+
			"\\p{Po}"+
			"\\p{Zs}"+
			"\\p{Zl}"+
			"\\p{Zp}"+
			"\\p{Cc}"+
			"\\p{Cf}"+
			"\\p{Cs}"+
			"‘|~"+
	"]+");

	public static boolean filterBasic(String s){
		return BASIC_TOKEN_FILTER.matcher(s).matches();
	}

	public static List<String> filterTokensBasic(List<String> tokens){
		List<String> filtered=new ArrayList<String>();
		for(String token:tokens){
			if(!filterBasic(token)){
				filtered.add(token);
			}
		}
		return filtered;
	}

	public static List<String> filterWhiteSpace(List<String> tokens){
		List<String> filtered=new ArrayList<String>();
		for(String token:tokens){
			if(token.trim().length()>0){
				filtered.add(token);
			}
		}
		return filtered;
	}

	public static List<String> filterTokens(List<String> tokens,Set<String> filter){
		List<String> filtered=new ArrayList<String>();
		for(String token:tokens){
			if(!filter.contains(token)){
				filtered.add(token);
			}
		}
		return filtered;
	}

	public static List<String> filterTokens(List<String> tokens,List<String> filter){
		return filterTokens(tokens,new HashSet<String>(filter));
	}

	public static List<String> filterTokens(List<String> tokens,String[] filter){
		return filterTokens(tokens,Arrays.asList(filter));
	}

	public static String tokensToString(List<String> tokens,String delim){
		StringBuilder b=new StringBuilder();
		for(int i=0;i<tokens.size();i++){
			b.append(tokens.get(i));
			if(i<tokens.size()-1){
				b.append(delim);
			}
		}
		return b.toString();
	}

	//Levenshtein Distance algorithm : modified version of Java version from http://en.wikipedia.org/wiki/Levenshtein_distance

	private static int minimum(int a,int b,int c){
		return Math.min(Math.min(a,b),c);
	}

	public static int getLevenshteinDistance(char[] str1,char[] str2){
		int[][] distance=new int[str1.length+1][];
		for(int i=0;i<=str1.length;i++){
			distance[i]=new int[str2.length+1];
			distance[i][0]=i;
		}
		for(int j=0;j<str2.length+1;j++){
			distance[0][j]=j;
		}
		for(int i=1;i<=str1.length;i++){
			for(int j=1;j<=str2.length;j++){
				distance[i][j]=minimum(distance[i-1][j]+1,distance[i][j-1]+1,distance[i-1][j-1]+((str1[i-1]==str2[j-1])?0:1));
			}
		}
		return distance[str1.length][str2.length];
	}

	public static int getLevenshteinDistance(String s1,String s2){
		return getLevenshteinDistance(s1.toCharArray(),s2.toCharArray());
	}

	public static List<String> chunk(String s){
		List<String> chunks=new ArrayList<String>();
		String[] split=s.split("\\.+");
		for(String chunk:split){
			chunks.add(chunk.trim());
		}
		return chunks;
	}

	public static String pad(String s,char padChar,int leftPadSize,int rightPadSize){
		StringBuilder b=new StringBuilder();
		for(int i=0;i<leftPadSize;i++){
			b.append(padChar);
		}
		b.append(s);
		for(int i=0;i<rightPadSize;i++){
			b.append(padChar);
		}
		return b.toString();
	}

	public static String padLeft(String s,char padChar,int paddedSize){
		if(s.length()>=paddedSize){
			return s;
		}
		else{
			return pad(s,padChar,paddedSize-s.length(),0);
		}
	}

	public static String padRight(String s,char padChar,int paddedSize){
		if(s.length()>=paddedSize){
			return s;
		}
		else{
			return pad(s,padChar,0,paddedSize-s.length());
		}
	}

	public static String padSides(String s,char padChar,int paddedSize){
		if(s.length()>=paddedSize){
			return s;
		}
		else{
			int left=(paddedSize-s.length())/2;
			int right=paddedSize-s.length()-left;
			return pad(s,padChar,left,right);
		}
	}
	
	public static Set<String> loadStopwords(String file,boolean caseSensitive){
		try{
			Set<String> stopwords=new HashSet<String>();
			BufferedReader reader=IOUtil.getBufferedReader(file,"utf8");
			for(String line;(line=reader.readLine())!=null;){
				line=line.trim();
				if(line.length()>0&&!line.startsWith("#")){
					if(!caseSensitive){
						line=line.toLowerCase();
					}
					stopwords.add(line);
				}
			}
			reader.close();
			return stopwords;
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static int nthIndexOf(String s,char c,int n){
		int count=0;
		int curr=-1;
		for(int i=0;i<s.length()&&count<n;i++){
			if(s.charAt(i)==c){
				count++;
				curr=i;
			}
		}
		if(count==n){
			return curr;
		}
		else{
			return -1;
		}
	}
	
	public static void main(String[] args){
		
		String test="\tHi I am very\tsad\ttoday\t";
		
		System.out.println(nthIndexOf(test,'\t',0));
		System.out.println(nthIndexOf(test,'\t',1));
		System.out.println(nthIndexOf(test,'\t',2));
		System.out.println(nthIndexOf(test,'\t',3));
		System.out.println(nthIndexOf(test,'\t',4));
		System.out.println(nthIndexOf(test,'\t',5));
		
	}

}
