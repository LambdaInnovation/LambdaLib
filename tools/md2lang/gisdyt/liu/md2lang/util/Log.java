package gisdyt.liu.md2lang.util;

public class Log {

	public static boolean log=false;
	
	public static void print(String s){
		if(log) System.out.print(s);
	}
	
	public static void println(String s){
		if(log) System.out.println(s);
	}
}
