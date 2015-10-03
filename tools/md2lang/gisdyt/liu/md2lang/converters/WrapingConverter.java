package gisdyt.liu.md2lang.converters;

import gisdyt.liu.md2lang.util.Converter;

/*
 * Like Bold, Emphasize and Strike-through.
 * I think my solution is very foolish. The Escape.. and Wraping...
 */
@Converter(priority=1)
public class WrapingConverter {

	private static final String[][] wrapedLables=new String[][]{
		{"~~", "~~", "[stth]", "[/stth]"},
		{"```", "```", "[bold]", "[/bold]"},
		{"__", "__", "[bold]", "[/bold]"},
		{"`", "`", "[code]", "[/code]"},
		{"==", "==", "[hili]", "[/hili]"},
	};
	
	public static String convert(String s){
		for(int i=0;i<wrapedLables.length;++i){
			int begin_index;
			while(s.indexOf(wrapedLables[i][0])!=-1 && s.indexOf(wrapedLables[i][1])!=-1){
				s=s.replaceFirst(wrapedLables[i][0], wrapedLables[i][2]);
				s=s.replaceFirst(wrapedLables[i][1], wrapedLables[i][3]);
			}
		}
		return s;
	}
}
