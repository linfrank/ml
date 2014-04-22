package edu.cmu.cs.frank.util;



public class ParametersTest{

	public static enum Zodiac{
		ARIES,
		TAURUS,
		GEMINI,
		CANCER,
		LEO,
		VIRGO,
		LIBRA,
		SCORPIO,
		SAGITTARIUS,
		CAPRICORN,
		AQUARIUS,
		PISCES
	}

	public static class Params extends Parameters{

		static final long serialVersionUID=20080513L;
		
		public String name;
		public int age=25;
		public double height=5.66;
		public Zodiac sign;

		public Params(){
			definePrintName("name","Name");
			definePrintName("age","Age");
			definePrintName("height","Height");
			definePrintName("sign","Zodiac Sign");
			defineLowerBound("age",0,true);
			defineLowerBound("height",3,true);
		}
	}

	public static void main(String[] args){

		Params p=new Params();
		
		p.name="Frank Lin";
		p.age=29;
		p.sign=Zodiac.AQUARIUS;

		System.out.println(p);

		p.check();
		
		p.parse("name","Frank");
		p.parse("age","15");
		p.parse("height","6.0");
		p.parse("sign","LEO");
		
		System.out.println(p);
		
		try{
			p.parse("age","-1");
		}catch(Exception e){
			System.out.println(e);
		}

	}
	
}