package gisdyt.liu.md2lang.util;

import gisdyt.liu.md2lang.converters.Converter;

import java.awt.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

public class ConverterLoader {

	public static ConverterLoader instance;
	private Class clazz;
	private static final String LIST_FILE="/converter_list.ls";
	
	static{
		instance=new ConverterLoader();
	}
	
	private ConverterLoader(){
		clazz=this.getClass();
	}
	
	public ArrayList<String> readList() throws IOException{
		BufferedReader br=new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(LIST_FILE)));
		String temp;
		ArrayList<String> list=new ArrayList<String>();
		while((temp=br.readLine())!=null){
			list.add(temp);
		}
		br.close();
		return list;
	}
	
	public void load() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException{
		ArrayList<String> list=readList();
		Iterator<String> iter=list.iterator();
		while(iter.hasNext()){
			Converter temp=(Converter) Class.forName(iter.next()).newInstance();
			Converters.converters.add(temp);
			Log.println("Loading Converter ["+temp.getClass().getName()+"]");
		}
	}
}
