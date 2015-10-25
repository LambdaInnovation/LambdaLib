package gisdyt.liu.md2lang.converters;

public interface Converter {
	
	public int getPriority();
	public String convert(String s);
}
