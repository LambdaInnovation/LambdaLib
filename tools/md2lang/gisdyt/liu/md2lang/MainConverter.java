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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.omg.PortableInterceptor.SUCCESSFUL;

import gisdyt.liu.md2lang.converters.Converter;
import gisdyt.liu.md2lang.util.ConverterLoader;
import gisdyt.liu.md2lang.util.Converters;
import gisdyt.liu.md2lang.util.Log;

//functions: 加粗 字体大小 图片及其大小控制 finished
//image: ![url](width, height)
//图片把![url](width, height)改成[img src="xxxx" width=123 height=456][/img]
public class MainConverter {

	public static boolean success;
	
	public static String convertMD2Lang(String md){
		success=true;
		try {
			ConverterLoader.instance.load();
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | IOException e) {
			// TODO Auto-generated catch block
			Log.printStackTrace(e);
		}
		Converters.converters.sort(new Comparator<Converter>() {

			@Override
			public int compare(Converter o1, Converter o2) {
				// TODO Auto-generated method stub
				return o1.getPriority()-o2.getPriority();
			}
		});
		Iterator<Converter> iter=Converters.converters.iterator();
		while(iter.hasNext()){
			Converter next=iter.next();
			md=next.convert(md);
			Log.println("Using converter ["+next.getClass().getName()+"]");
		}
		return md;
	}
}
