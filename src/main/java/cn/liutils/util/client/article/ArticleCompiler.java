package cn.liutils.util.client.article;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import cn.annoreg.core.Registrant;
import cn.annoreg.mc.RegInit;
import cn.liutils.core.LIUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

@Registrant
class ArticleCompiler {
	
	// Metainfo
	final String path;
	
	// current parsing char
	boolean isEOF = false;
	char current;
	
	int readed = 0;
	final Reader streamReader;
	final PushbackReader reader;
	
	Stack<Tag> parameterTags = new Stack<Tag>();
	
	ArticlePlotter plotter;
	
	public ArticleCompiler(String _path, InputStream stream) {
		path = _path;
		streamReader = new InputStreamReader(stream);
		reader = new PushbackReader(streamReader);
	}
	
	/**
	 * Compile the desired content into the ArticlePlotter.
	 */
	ArticlePlotter compile() throws IOException, LangCompileException {
		plotter = new ArticlePlotter();
		parse();
		return plotter;
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
	
	/**
	 * Use the current content parameter and current tag context to draw a line of string.
	 */
	private void drawContent(String content) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("content", content);
		NBTTagList tags = new NBTTagList();
		for(Tag t : parameterTags) {
			tags.appendTag(new NBTTagString(t.id));
		}
		tag.setTag("tags", tags);
		plotter.insr(Opcodes.TEXT, tag);
	}
	
	private void log(String message) {
		LIUtils.log.info("LCP: " + message);
	}
	
	public static class LangCompileException extends RuntimeException {
		
		public LangCompileException(ArticleCompiler compiler, String what, Exception e) {
			super(errstr(compiler, what), e);
		}
		
		public LangCompileException(ArticleCompiler compiler, String what) {
			super(errstr(compiler, what));
		}
		
		private static String errstr(ArticleCompiler compiler, String msg) {
        	return errstr(compiler) + ": " + msg;
        }
        
        private static String errstr(ArticleCompiler compiler) {
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
		
		String src;
		int width, height;
		
		TagImg() {
			super("img", true);
		}
		
		@Override
		public void updateInfo(String propName, String content) {
			switch(propName) {
			case "src":
				src = content;
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
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("src", src);
			tag.setInteger("width", width);
			tag.setInteger("height", height);
			plotter.insr(Opcodes.IMAGE, tag);
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
			plotter.insr(Opcodes.NEWLINE, null);
		}
		
		@Override
		public TagNewline copy() {
			return new TagNewline();
		}
	}
	
}
