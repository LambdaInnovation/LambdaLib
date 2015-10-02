package gisdyt.liu.md2lang.converters;

import gisdyt.liu.md2lang.util.Converter;

//图片把![url](width, height)改成[img src="xxxx" width=123 height=456][/img]
@Converter(priority=0)
public class ImageLinkinConverter {

	public static String convert(String s){
		int begin_index;
		while((begin_index=s.indexOf("!["))!=-1){
			int url_end_index=s.indexOf(']', begin_index);
			String url=s.substring(begin_index+2, url_end_index);
			int spliter_index=s.indexOf(',', url_end_index);
			int width=Integer.parseInt(s.substring(url_end_index+2, spliter_index).trim());
			int end_index=s.indexOf(')', spliter_index);
			int height=Integer.parseInt(s.substring(spliter_index+1, end_index).trim());
			String temp="[img src=\""+url+"\" width="+width+" height="+height+"]";
			String old_image=s.substring(begin_index, end_index+1);
			old_image=old_image.replaceAll("\\[", "\\\\[");
			old_image=old_image.replaceAll("\\(", "\\\\(");
			old_image=old_image.replaceAll("\\]", "\\\\]");
			old_image=old_image.replaceAll("\\)", "\\\\)");
			s=s.replaceAll(old_image, temp);
		}
		return s;
	}
}
