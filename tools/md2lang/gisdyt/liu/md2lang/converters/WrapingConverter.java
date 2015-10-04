package gisdyt.liu.md2lang.converters;

public class WrapingConverter implements Converter{

	@Override
	public int getPriority() {
		// TODO Auto-generated method stub
		return 1;
	}

	private static final String[][] wrapedLables=new String[][]{
		{"~~", "~~", "[stth]", "[/stth]"},
		{"```", "```", "[bold]", "[/bold]"},
		{"__", "__", "[bold]", "[/bold]"},
		{"`", "`", "[code]", "[/code]"},
		{"==", "==", "[hili]", "[/hili]"}
	};
	
	public String convert(String s){
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
