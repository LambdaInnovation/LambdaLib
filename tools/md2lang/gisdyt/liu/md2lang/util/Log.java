package gisdyt.liu.md2lang.util;

import gisdyt.liu.md2lang.GUIMain;
import gisdyt.liu.md2lang.MainConverter;

public class Log {

	public static boolean log=false;
	public static boolean info=true;
	
	public static void print(String s){
		if(log) System.out.print(s);
	}
	
	public static void println(String s){
		if(log) System.out.println(s);
	}
	
	public static void printStackTrace(Throwable e){
		if(log) e.printStackTrace();
		GUIMain.message_exception(e);
		MainConverter.success=false;
	}
	
	public static void info(String s){
		if(info) System.out.print(s);
	}
	
	public static void infoln(String s){
		if(info) System.out.println(s);
	}
}
