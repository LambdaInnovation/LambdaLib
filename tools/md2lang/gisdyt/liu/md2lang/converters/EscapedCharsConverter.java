package gisdyt.liu.md2lang.converters;

import gisdyt.liu.md2lang.util.Converter;

/*
 * This converter will
 */
@Converter(priority=2)
public class EscapedCharsConverter {

	private static final String replacable[][]=new String[][]{
			{"&nbsp;", " "},
			{"-", ""},
			{"&equa;", "="},
			{"&hyph;", "-"},
			{"\n\\*\n", "[npar]"},
			{"\t", "&tab;"},
			{"  \n", "[ln]"},
			{"\n\n", "[ln]"}
	}; 
	
	public static String convert(String s){
		for(int i=0;i<replacable.length;++i){
			s=s.replaceAll(replacable[i][0], replacable[i][1]);
		}
		return s;
	}
}
