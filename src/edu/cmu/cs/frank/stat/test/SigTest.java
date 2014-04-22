package edu.cmu.cs.frank.stat.test;

import java.io.File;
import java.util.List;

import edu.cmu.cs.frank.ml.classify.Evaluation;
import edu.cmu.cs.frank.util.IOUtil;


public abstract class SigTest{
	
	protected double twoTail=Double.NaN;
	protected double oneTail=Double.NaN;
	protected double leftTail=Double.NaN;
	protected double rightTail=Double.NaN;

	public abstract void calculate(Evaluation e1,Evaluation e2);
	
	public String getTestResult(SigTest st,File evalFile1,File evalFile2)throws Exception{
		Evaluation e1=(Evaluation)IOUtil.readObject(evalFile1);
		Evaluation e2=(Evaluation)IOUtil.readObject(evalFile2);
		calculate(e1,e2);
		StringBuilder b=new StringBuilder();
		b.append("p-values:").append("\n");
		b.append(" Two-Tail: ").append(twoTail).append("\n");
		b.append(" One-Tail: ").append(oneTail).append("\n");
		b.append(" Left-Tail: ").append(leftTail).append("\n");
		b.append(" Right-Tail: ").append(rightTail).append("\n");
		return b.toString();
	}

	public static void main(String[] args)throws Exception{

		if(args.length!=3){
			System.out.println("Usage:");
			System.out.println("java "+SigTest.class.getSimpleName()+" TEST_NAME EVAL_FILE1 EVAL_FILE2");
			System.out.println("java "+SigTest.class.getSimpleName()+" TEST_NAME EVAL_DIR EVAL_FILES_FILE");
			return;
		}
		
		Class<? extends SigTest> clazz=Class.forName(SigTest.class.getPackage().getName()+"."+args[0]).asSubclass(SigTest.class);
		SigTest st=clazz.newInstance();
		
		File arg1=new File(args[1]);
		File arg2=new File(args[2]);

		if(arg1.isDirectory()){
			List<String> lines=IOUtil.readLines(arg2,"utf8");
			for(int i=0;i<lines.size();i++){
				String[] data=lines.get(i).split("\\s+");
				File evalFile1=new File(arg1+"/"+data[0]);
				File evalFile2=new File(arg1+"/"+data[1]);
				System.out.println(st.getTestResult(st,evalFile1,evalFile2));
			}
		}
		else{
			System.out.println(st.getTestResult(st,arg1,arg2));
		}

	}

}
