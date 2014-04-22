package edu.cmu.cs.frank.util;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class Parameters implements Serializable{

	static final long serialVersionUID=20080512L;

	private Map<String,String> printNames=new HashMap<String,String>();
	private Map<String,Double> incLowerBounds=new HashMap<String,Double>();
	private Map<String,Double> incUpperBounds=new HashMap<String,Double>();
	private Map<String,Double> excLowerBounds=new HashMap<String,Double>();
	private Map<String,Double> excUpperBounds=new HashMap<String,Double>();

	public Field[] getFields(){
		return this.getClass().getFields();
	}

	public String[] getNames(){
		Field[] fields=this.getFields();
		String[] names=new String[fields.length];
		for(int i=0;i<fields.length;i++){
			names[i]=fields[i].getName();
		}
		return names;
	}

	public Object getValue(String name){
		try{
			return findField(name).get(this);
		}catch(IllegalAccessException iae){
			iae.printStackTrace();
			return null;
		}
	}

	public void setValue(String name,Object value){
		try{
			findField(name).set(this,value);
		}catch(IllegalAccessException iae){
			iae.printStackTrace();
		}
	}

	// name you would like to see in toString or GUI
	public void definePrintName(String paramName,String printName){
		printNames.put(paramName,printName);
	}

	// inclusive
	public void defineLowerBound(String paramName,double bound,boolean inclusive){
		if(inclusive){
			incLowerBounds.put(paramName,bound);
		}
		else{
			excLowerBounds.put(paramName,bound);
		}
	}

	// inclusive
	public void defineUpperBound(String paramName,double bound,boolean inclusive){
		if(inclusive){
			incUpperBounds.put(paramName,bound);
		}
		else{
			excUpperBounds.put(paramName,bound);
		}
	}

	public void check(Field field)throws ParameterException{
		try{
			String name=field.getName();
			Type type=field.getType();
			if(type.equals(Integer.TYPE)){
				Integer value=field.getInt(this);
				Double bound=null;
				if((bound=incLowerBounds.get(name))!=null&&value<bound){
					throw new ParameterException(name,value,"Must be greater than or equal to "+bound);
				}
				if((bound=excLowerBounds.get(name))!=null&&value<=bound){
					throw new ParameterException(name,value,"Must be greater than "+bound);
				}
				if((bound=incUpperBounds.get(name))!=null&&value>bound){
					throw new ParameterException(name,value,"Must be less than or equal to "+bound);
				}
				if((bound=excUpperBounds.get(name))!=null&&value>=bound){
					throw new ParameterException(name,value,"Must be less than "+bound);
				}
			}
			else if(type.equals(Double.TYPE)){
				Double value=field.getDouble(this);
				Double bound=null;
				if((bound=incLowerBounds.get(name))!=null&&value<bound){
					throw new ParameterException(name,value,"Must be greater than or equal to "+bound);
				}
				if((bound=excLowerBounds.get(name))!=null&&value<=bound){
					throw new ParameterException(name,value,"Must be greater than "+bound);
				}
				if((bound=incUpperBounds.get(name))!=null&&value>bound){
					throw new ParameterException(name,value,"Must be less than or equal to "+bound);
				}
				if((bound=excUpperBounds.get(name))!=null&&value>=bound){
					throw new ParameterException(name,value,"Must be less than "+bound);
				}
			}
		}catch(IllegalAccessException iae){
			iae.printStackTrace();
		}
	}

	public void check()throws ParameterException{
		Field[] fields=this.getFields();
		for(int i=0;i<fields.length;i++){
			check(fields[i]);
		}
	}

	private Field findField(String paramName){
		Field[] fields=this.getFields();
		for(int i=0;i<fields.length;i++){
			if(fields[i].getName().equals(paramName)){
				return fields[i];
			}
		}
		return null;
	}

	public void parse(String paramName,String input)throws ParameterException{
		Field field=findField(paramName);
		if(field==null){
			throw new ParameterException("No such parameter: "+paramName);
		}
		else{
			Class<?> type=field.getType();
			try{
				if(type.equals(Boolean.TYPE)){
					field.setBoolean(this,Boolean.parseBoolean(input));
				}
				else if(type.equals(Double.TYPE)){
					field.setDouble(this,Double.parseDouble(input));
				}
				else if(type.equals(Float.TYPE)){
					field.setFloat(this,Float.parseFloat(input));
				}
				else if(type.equals(Integer.TYPE)){
					field.setInt(this,Integer.parseInt(input));
				}
				else if(type.equals(Long.TYPE)){
					field.setLong(this,Long.parseLong(input));
				}
				else if(type.equals(String.class)){
					field.set(this,input);
				}
				else if(type.isEnum()){
					Object[] constants=type.getEnumConstants();
					for(int i=0;i<constants.length;i++){
						Enum<?> constant=(Enum<?>)constants[i];
						if(constant.name().equalsIgnoreCase(input)){
							field.set(this,constant);
						}
					}
				}
				else if(type.isArray()){
					Class<?> compType=type.getComponentType();
					String[] tokens=input.trim().split(" *, *");
					Object array=Array.newInstance(compType,tokens.length);
					for(int i=0;i<tokens.length;i++){
						if(compType.equals(Boolean.TYPE)){
							Array.setBoolean(array,i,Boolean.parseBoolean(tokens[i]));
						}
						else if(compType.equals(Double.TYPE)){
							Array.setDouble(array,i,Double.parseDouble(tokens[i]));
						}
						else if(compType.equals(Float.TYPE)){
							Array.setFloat(array,i,Float.parseFloat(tokens[i]));
						}
						else if(compType.equals(Integer.TYPE)){
							Array.setInt(array,i,Integer.parseInt(tokens[i]));
						}
						else if(compType.equals(Long.TYPE)){
							Array.setLong(array,i,Long.parseLong(tokens[i]));
						}
						else if(compType.equals(String.class)){
							Array.set(array,i,tokens[i]);
						}
						else{
							System.err.println("Unsupported array parameter type: "+type);
						}
					}
					field.set(this,array);
				}
				else{
					System.err.println("Unsupported parameter type: "+type);
				}
			}catch(IllegalAccessException iae){
				iae.printStackTrace();
			}
			check(field);
		}
	}

	public String getPrintName(String paramName){
		if(printNames.containsKey(paramName)){
			return printNames.get(paramName);
		}
		else{
			return paramName;
		}
	}

	@Override
	public String toString(){
		StringBuilder b=new StringBuilder();
		b.append(this.getClass().getCanonicalName()).append(" = {\n");
		Field[] fields=this.getFields();
		for(int i=0;i<fields.length;i++){
			String paramName=fields[i].getName();
			b.append(" ").append(getPrintName(paramName)).append(" = ");
			try{
				b.append(fields[i].get(this)).append("\n");
			}catch(IllegalAccessException iae){
				iae.printStackTrace();
			}
		}
		b.append("}");
		return b.toString();
	}

	private static boolean isDescendantOf(Class<?> d,Class<?> a){
		Class<?> sup=d.getSuperclass();
		if(sup==null){
			return false;
		}
		else if(sup.equals(a)){
			return true;
		}
		else{
			return isDescendantOf(sup,a);
		}
	}

	public static Parameters getDefault(Class<? extends Parametizable> clazz){
		Class<?>[] classes=clazz.getDeclaredClasses();
		for(int i=0;i<classes.length;i++){
			if(
					classes[i].getSimpleName().equalsIgnoreCase("Params")&&
					isDescendantOf(classes[i],Parameters.class)
			){
				Object o;
				try{
					o=classes[i].newInstance();
				}catch(InstantiationException ie){
					ie.printStackTrace();
					return null;
				}catch(IllegalAccessException iae){
					iae.printStackTrace();
					return null;
				}
				return (Parameters)o;
			}
		}
		System.err.println("Cannot find default parameters for "+clazz);
		return null;
	}

	public Class<?> getEnclosingClass(){
		return getClass().getEnclosingClass();
	}

	public static Parametizable newInstance(Class<? extends Parametizable> clazz,Parameters params){
		try{
			Parametizable parametizable=clazz.newInstance();
			parametizable.setParams(params);
			return parametizable;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static Parametizable newInstance(Class<? extends Parametizable> clazz){
		return newInstance(clazz,getDefault(clazz));
	}

	@SuppressWarnings("unchecked")
	public static Parametizable newInstance(Parameters params){
		return newInstance((Class<? extends Parametizable>)params.getEnclosingClass(),params); 
	}

	public class ParameterException extends IllegalArgumentException{

		static final long serialVersionUID=20080506L;

		public ParameterException(String message){
			super(message);
		}

		public ParameterException(String paramName,Object value,String message){
			this("Bad: "+paramName+" = "+value+" ("+message+")");
		}

	}

	public class Choice{

		private List<Parameters> choices;
		int chosen;

		public Choice(List<Parameters> choices){
			this.choices=choices;
			chosen=0;
		}

		public Choice(Parameters...choices){
			this(Arrays.asList(choices));
		}

		public List<Parameters> getChoices(){
			return choices;
		}

		public Parameters getChosen(){
			return choices.get(chosen);
		}

		public void setChosen(int chosen){
			this.chosen=chosen;
		}

		public void changeChoice(int i,Parameters params){
			choices.set(i,params);
		}

		@Override
		public String toString(){
			StringBuilder b=new StringBuilder();
			b.append(chosen).append(" = ").append(getChosen());
			return b.toString();
		}

	}

}
