package gisdyt.liu.md2lang.converters;

import gisdyt.liu.md2lang.util.Converter;

/*
 * There isn't any tab like heading, so I make it as special.
 * I know that's foolish. We need to decoupling.
 */
@Converter(priority=1)
public class HeadingConverter {

	public static String convert(String s){
		s=sharp(s);
		s=lnsh(s);
		return s;
	}
	
	public static String sharp(String s){
		int first_sharp_index;
		while((first_sharp_index=s.indexOf('#'))!=-1){
			int begin_index=first_sharp_index;
			int end_index=s.indexOf('\n', begin_index);
			String source=s.substring(begin_index, end_index);
			int last_sharp_index=s.lastIndexOf('#', end_index);
			int heading_level=last_sharp_index-first_sharp_index+1;
			String tab_head="[h"+heading_level+"]";
			char temp;
			int content_begin_index=-1;
			for(int i=0;i<=source.length();++i){
				temp=source.charAt(i);
				if(temp!=' ' && temp!='#'){
					content_begin_index=i;
					break;
				}
			}
			String tab_bottom="[/h"+heading_level+"]";
			String tab_content=tab_head+source.substring(content_begin_index)+tab_bottom;
			s=s.replaceAll(source, tab_content);
		}
		return s;
	}
	
	public static String lnsh(String s){
		int first_sh_index;
		while((first_sh_index=s.indexOf("\n-"))!=-1){
			s=s.substring(0, first_sh_index)+"[/h1]"+s.substring(first_sh_index);
			int last_ln_index=s.lastIndexOf('\n', first_sh_index-1);
			s=s.substring(0, last_ln_index+1)+"[h1]"+s.substring(last_ln_index+1);
			s=s.replaceFirst("\n-", "");
		}
		return s;
	}
}
