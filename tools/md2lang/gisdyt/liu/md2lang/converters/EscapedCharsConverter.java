package gisdyt.liu.md2lang.converters;

public class EscapedCharsConverter implements Converter{
	
	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 2;
	}

	private static final String replacable[][]=new String[][]{
			{"&nbsp;", " "},
			{"-", ""},
			{"=", ""},
			{"&equa;", "="},
			{"&hyph;", "-"},
			{"\n\\*\n", "[npar]"},
			{"\t", "&tab;"},
			{"  \n", "[ln]"},
			{"\n\n", "[ln]"},
			{"\n", ""}
	}; 
	
	public String convert(String s){
		for(int i=0;i<replacable.length;++i){
			s=s.replaceAll(replacable[i][0], replacable[i][1]);
		}
		return s;
	}
}
