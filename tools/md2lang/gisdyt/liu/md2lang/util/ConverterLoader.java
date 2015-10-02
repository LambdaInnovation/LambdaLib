package gisdyt.liu.md2lang.util;

import java.util.ArrayList;

public class ConverterLoader {

	public static void load(){
		Converters.converters=(ArrayList<Class<?>>) ClassesSearcher.searchByAnnotation("gisdyt.liu.md2lang.converters", Converter.class, true);
	}
}
