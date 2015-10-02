package gisdyt.liu.md2lang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import gisdyt.liu.md2lang.util.Converter;
import gisdyt.liu.md2lang.util.ConverterLoader;
import gisdyt.liu.md2lang.util.Converters;
import gisdyt.liu.md2lang.util.Log;

//functions: 加粗 字体大小 图片及其大小控制 finished
//image: ![url](width, height)
//图片把![url](width, height)改成[img src="xxxx" width=123 height=456][/img]
public class MainConverter {

	public static String convertMD2Lang(String md){
		ConverterLoader.load();
		String result=md;
		Converters.converters.sort(new Comparator<Class<?>>() {

			@Override
			public int compare(Class<?> o1, Class<?> o2) {
				// TODO Auto-generated method stub
				int i1=((Converter) o1.getAnnotation(Converter.class)).priority();
				int i2=((Converter) o2.getAnnotation(Converter.class)).priority();
				return i1-i2;
			}
		});
		for(int i=0;i<Converters.converters.size();++i){
			try {
				result=(String) Converters.converters.get(i).getMethod("convert", String.class).invoke(null, result);
				Log.println("Using converter ["+Converters.converters.get(i).getName()+"]");
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private static void file_converting(String in, String out) throws Exception{
		System.out.println("Converting is starting...");
		File input=new File(in);
		File output=new File(out);
		if(input.exists()==false) System.err.println("Input file not found.");
		if(output.exists()==true) {System.err.println("Output has already exists. I will delete it."); output.delete();}
		output.createNewFile();
		BufferedReader br=new BufferedReader(new FileReader(input));
		StringBuffer buffer=new StringBuffer();
		String temp;
		while((temp=br.readLine())!=null){
			buffer.append(temp);
		}
		br.close();
		System.out.println("Reading input file finished...");
		String md=buffer.toString();
		String result=convertMD2Lang(md);
		System.out.println("Converting finished...");
		PrintWriter pw=new PrintWriter(output);
		pw.print(result);
		pw.flush();
		pw.close();
		System.out.println("Writing result finished...");
		System.out.println("MD2Lang process finished successfully! Thanks for your using.");
	}
	
	private static void please_ask_WeAthFolD(){
		System.out.println("There should be a helping documentation but it is omitted. If you have any questions, please ask the group leader of LI, WeAthFolD.");
	}
	
	public static void main(String[] args) throws Exception{
		if(args.length==0){
			System.out.println("Welcome to the MD2Lang. It is writed by Gisdyt@LI.");
			System.out.println("There should be a description but it is omitted.");
			please_ask_WeAthFolD();
			System.out.println("I just describe the calling grammar.");
			System.out.println("java -jar md2lang.jar <input_file> <output_file> [--stacktrace]");
			System.out.println("And now you can enjoy it.");
		}else if(args.length==2){
			file_converting(args[0], args[1]);
		}else if(args.length==3 && args[2].equals("--stacktrace")){
			Log.log=true;
			file_converting(args[0], args[1]);
		}else if(args[0].equals("-h") || args[0].equals("/?") || args[0].equals("--help") || args[0].equals("-?") || args[0].equals("/h")){
			please_ask_WeAthFolD();
		}else{
			System.out.println("You use a wrong grammar.");
			please_ask_WeAthFolD();
		}
	}
}
