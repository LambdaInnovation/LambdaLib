package cn.liutils.util.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import cn.annoreg.core.Registrant;
import cn.annoreg.mc.RegInit;
import cn.liutils.core.LIUtils;
import net.minecraft.util.ResourceLocation;

@Registrant
@RegInit
public class FormattedLangCompiler {
	
	// Test
	public static void init() {
		try {
			new FormattedLangCompiler("test", 
				new ByteArrayInputStream(
						"[h1]This is a test[/h1][ln][bold]This is another test[/bold]line[ln][img src=\"academy:textures/tutorial/xxx.png\" width=123 height=456][ln]I tell you this is good!"
						.getBytes())).compile();
		} catch (LangCompileException|IOException e) {
			e.printStackTrace();
		}
	}
	
	// Rendering Options
	public double fontSize = 10;
	public double fontSize_h1 = 20;
	public double fontSize_h2 = 18;
	public double fontSize_h3 = 16;
	public double fontSize_h4 = 14;
	
	public double rowSpacing = 2;
	
	// Metainfo
	final String path;
	
	// 
	boolean isEOF = false;
	char current;
	
	int readed = 0;
	final Reader streamReader;
	final PushbackReader reader;
	
	Stack<Tag> parameterTags = new Stack<Tag>();
	
	double curx, cury;
	
	public FormattedLangCompiler(String _path, InputStream stream) {
		path = _path;
		streamReader = new InputStreamReader(stream);
		reader = new PushbackReader(streamReader);
	}
	
	/**
	 * Compile the desired content into a GL draw list and then return the list id.
	 */
	public int compile() throws IOException, LangCompileException {
		
		initDrawContext();
		parse();
		endDrawContext();
		
		return 0;
	}
	
	// Parsing
	private char read() throws IOException {
		int x = reader.read();
		if(x == -1 || !reader.ready()) {
			isEOF = true;
		}
		current = (char) x;
		++readed;
		return current;
	}
	
    private char peekChar() throws IOException {
        char c = (char) reader.read();
        reader.unread(c);
        return c;
    }
	
	private void parse()  throws IOException {
		while(!isEOF) {
			if(peekChar() == '[') {
				parseTag();
			} else {
				parseContent();
			}
		}
	}
	
	private void parseTag() throws IOException {
		if(read() != '[')
			throw new LangCompileException(this, "Tag doesn't start with '['!");
		
		String[] tagInfo = parseUntil(']').split(" ");
		Tag tag = tags.get(tagInfo[0]);
		if(tag == null)
			throw new LangCompileException(this, "No tag with name " + tagInfo[0]);
		tag = tag.copy();
		for(int i = 1; i < tagInfo.length; ++i) {
			String prop = tagInfo[i];
			int ind = prop.indexOf('=');
			String id = prop.substring(0, ind);
			String val = prop.substring(ind + 1, prop.length());
			if(val.charAt(0) == '"' && val.charAt(val.length() - 1) == '"') {
				val = val.substring(1, val.length() - 1);
			}
			tag.updateInfo(id, val);
		}
		if(read() != ']')
			throw new LangCompileException(this, "Tag doesn't end with ']'!");
		
		if(tag.inline)
			tag.inlineParsed();
		else {
			parameterTags.push(tag);
			parseContent();
			parseTagEnd(tag.id);
			parameterTags.pop();
		}
	}
	
	private void parseTagEnd(String id) throws IOException {
		if(read() != '[' || read() != '/')
			throw new LangCompileException(this, "Tag doesn't start with \"[/\"!");
		
		String parsedId;
		if(!(parsedId = parseUntil(']')).equals(id))
			throw new LangCompileException(this, "Invalid ending tag: should be " + id + ", found " + parsedId);
		
		read();
	}
	
	private String parseUntil(char token) throws IOException {
		StringBuilder content = new StringBuilder();
		while(peekChar() != token && !isEOF) {
			read();
			if(!isEOF)
				content.append(current);
		}
		return content.toString();
	}
	
	private void parseContent() throws IOException {
		drawContent(parseUntil('['));
	}
	
	// Rendering
	private void initDrawContext() {
		log("init draw context");
	}
	
	/**
	 * Use the current content parameter and current tag context to draw a line of string.
	 */
	private void drawContent(String content) {
		StringBuilder sb = new StringBuilder("draw \"");
		sb.append(content).append("\" ");
		for(Tag tag : parameterTags) {
			sb.append('[').append(tag.id).append(']');
		}
		log(sb.toString());
	}
	
	private void drawImage(ResourceLocation texture, int width, int height) {
		log("draw image " + texture + " (" + width + "," + height + ")");
	}
	
	private void endDrawContext() {
		log("end draw context");
	}
	
	private void log(String message) {
		LIUtils.log.info("LCP: " + message);
	}
	
	public static class LangCompileException extends RuntimeException {
		
		public LangCompileException(FormattedLangCompiler compiler, String what, Exception e) {
			super(errstr(compiler, what), e);
		}
		
		public LangCompileException(FormattedLangCompiler compiler, String what) {
			super(errstr(compiler, what));
		}
		
		private static String errstr(FormattedLangCompiler compiler, String msg) {
        	return errstr(compiler) + ": " + msg;
        }
        
        private static String errstr(FormattedLangCompiler compiler) {
        	return "at " + compiler.path + ", char " + compiler.readed;
        }
		
	}
	
	// Tags
	private class Tag {
		final String id;
		final boolean inline;
		
		Tag(String _id) {
			this(_id, false);
		}
		
		Tag(String _id, boolean _inline) {
			id = _id;
			inline = _inline;
			// Construct and add to the map
			tags.put(id, this);
		}
		
		void updateInfo(String propName, String content) {
			// DEFAULT: Nope
		}
		
		void inlineParsed() {}
		
		public <T extends Tag> T copy() {
			return (T) new Tag(id, inline);
		}
	}
	
	private Map<String, Tag> tags = new HashMap();
	
	private Tag
		tagNewline = new TagNewline(),
		tagBold = new Tag("bold"),
		tagH1 = new Tag("h1"),
		tagImage = new TagImg();
	
	private class TagImg extends Tag {
		
		ResourceLocation src;
		int width, height;
		
		TagImg() {
			super("img", true);
		}
		
		@Override
		public void updateInfo(String propName, String content) {
			switch(propName) {
			case "src":
				src = new ResourceLocation(content);
				break;
			case "width":
				width = Integer.valueOf(content);
				break;
			case "height":
				height = Integer.valueOf(content);
				break;
			}
		}
		
		@Override
		void inlineParsed() {
			drawImage(src, width, height);
		}
		
		@Override
		public TagImg copy() {
			return new TagImg();
		}
		
	}
	
	private class TagNewline extends Tag {
		TagNewline() {
			super("ln", true);
		}
		
		@Override
		void inlineParsed() {
			log("draw [newline]");
		}
		
		@Override
		public TagNewline copy() {
			return new TagNewline();
		}
	}
	
}
