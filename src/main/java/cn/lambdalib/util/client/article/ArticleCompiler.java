package cn.lambdalib.util.client.article;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.client.font.Fragmentor;
import cn.lambdalib.util.client.font.Fragmentor.TokenType;
import cn.lambdalib.util.client.font.IFont;
import cn.lambdalib.util.client.font.IFont.FontOption;
import cn.lambdalib.util.client.font.TrueTypeFont;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Registrant
public class ArticleCompiler {

	private static final String BOLD = "§l";
	private static final float headerScales[] = { 1.6f, 1.5f, 1.4f, 1.3f, 1.2f, 1.1f };
	private static final String headerPrefixes[] = { BOLD, BOLD, BOLD, "", "", "", };
	private static final Set<String> inlineTags = new HashSet<>();
	static {
		inlineTags.add("img");
		inlineTags.add("br");
	}

	private final PushbackReader reader;
	private ArticlePlotter plotter;
	private boolean eof = false;
	private char current;

	private StringBuilder unflushed = new StringBuilder();

	private Stack<String> tags = new Stack<>();

	// Render parameteres
	private IFont font = TrueTypeFont.defaultFont();
	private FontOption fontOption = new FontOption();

	private double spacing = 3, width = 2333;

	// Compile-time render states
	private double x = 0, y = 0, lfs;

	public ArticleCompiler(String str) {
		this(str, new ArticlePlotter());
	}

	public ArticleCompiler(InputStream stream) {
		this(stream, new ArticlePlotter());
	}

	public ArticleCompiler(String str, ArticlePlotter plotter) {
		this(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)), plotter);
	}

	public ArticleCompiler(InputStream stream, ArticlePlotter _plotter) {
		reader = new PushbackReader(new InputStreamReader(stream));
		plotter = _plotter;
	}

	// Rendering options
	public ArticleCompiler setFont(IFont _font) {
		font = _font;
		return this;
	}

	public ArticleCompiler setFontOption(FontOption _option) {
		fontOption = _option;
		return this;
	}

	public ArticleCompiler setSpacing(double _spacing) {
		spacing = _spacing;
		return this;
	}

	public ArticleCompiler setWidth(double _width) {
		width = _width;
		return this;
	}

	public ArticlePlotter compile() {
		try {
			parse();

			plotter.font = font;
			plotter.option = fontOption.clone();
		} catch(Exception e) {
			LambdaLib.log.fatal("Error compiling article", e);
		}
		return plotter;
	}

	// Internal
	private void parse() throws IOException {
		lfs = fontOption.fontSize;

		while(!eof) {
			if(peekChar() == '[') {
				parseTag();
			} else {
				parseContent();
			}
		}
		flushContent();
		reader.close();
	}

	private void parseTag() throws IOException {
		require(read() == '[');
		String content = parseUntil(']');
		PushbackReader pr = new PushbackReader(new StringReader(content));

		boolean tagend = false;

		if(content.charAt(0) == '/') {
			pr.read();
			tagend = true;
		}

		String tagname = parseUntil(pr, ' ', true);
		if(inlineTags.contains(tagname)) {
			// Parse tag parameters
			Map<String, String> pars = new HashMap<>();
			while(!isEOF(pr)) {
				String key = parseUntil(pr, '=');
				String value;
				char ch = (char) pr.read();
				if(ch == '"')  {
					value = parseUntil(pr, '"');
					require(read() == '"');
				} else {
					pr.unread(ch);
					value = parseUntil(pr, ' ', true);
				}

				int x = pr.read();
				require(x == ' ' || x == -1);

				pars.put(key, value);
			}
			onInlineTag(tagname, pars);
		} else {
			// Manipulate the tag stack
			if(tagend) {
				flushContent();
				require(tags.pop().equals(tagname));
				debug("TAGOT " + tagname);
			} else {
				flushContent();
				tags.push(tagname);
				debug("TAGIN " + tagname);
			}
		}

		require(read() == ']');

		pr.close();
	}

	private void parseContent() throws IOException {
		unflushed.append(parseUntil('[', true));
	}

	private String parseUntil(PushbackReader rdr, char token) throws IOException {
		return parseUntil(rdr, token, false);
	}

	private String parseUntil(PushbackReader rdr, char token, boolean relax) throws IOException {
		StringBuilder sb = new StringBuilder();
		boolean eof = false;
		while(!eof) {
			int x = rdr.read();
			if(x == -1) {
				eof = true;
			} else {
				char ch = (char) x;
				if(ch == token) {
					rdr.unread(ch);
					break;
				}
				sb.append(ch);
			}
		}
		require(!eof || relax, "Reached end-of-file. Expecting: " + token);
		return sb.toString();
	}

	private String parseUntil(char token, boolean relax) throws IOException {
		String ret = parseUntil(reader, token, relax);
		eof = !reader.ready();
		return ret;
	}

	private String parseUntil(char token) throws IOException {
		return parseUntil(reader, token);
	}

	private void onInlineTag(String name, Map<String, String> parameters) {
		switch(name) {
		case "img":
			flushContent();
			plotter.iImage(new ResourceLocation(parameters.get("src")),
					x, y,
					Integer.valueOf(parameters.get("width")),
					Integer.valueOf(parameters.get("height")));
			break;
		case "lb":
			unflushed.append('[');
			break;
		case "rb":
			unflushed.append(']');
			break;
		case "br":
			flushContent();
			x = 0;
			y += lfs + spacing;
			break;
		case "key":
			plotter.iKey(parameters.get("name"), x, y, 1);
			x += 20;
			break;
		default:
			fail("Invalid inline tag " + name);
		}
		debug("INL   " + name + " => " + parameters);
	}

	private void flushContent() {
		if(unflushed.length() != 0) {
			String content = unflushed.toString();

			StringBuilder sb = new StringBuilder();
			for(String tag : tags) {
				sb.append(" [").append(tag).append("]");
			}
			debug("FLUSH " + content + sb.toString());

			String pref = "";

			double size = fontOption.fontSize;
			for(String tag : tags) {
				if(tag.startsWith("h")) {
					int hid = Integer.valueOf(tag.substring(1)) - 1;
					require(hid >= 0 && hid < 6);
					size = fontOption.fontSize * headerScales[hid];
					pref = headerPrefixes[hid];
				} else if(tag.equals("em")) {
					pref = BOLD;
				}
			}

			Fragmentor f = new Fragmentor(content);
			while(f.hasNext()) {
				Pair<TokenType, String> pair = f.next();
				procFragment(pair.getLeft(), pref, pair.getRight(), size);
			}

			unflushed = new StringBuilder();
			lfs = size;
		}
	}

	private void procFragment(TokenType type, String pref, String str, double size) {
		debug("FRAG " + type + " " + str);
		double len = font.getTextWidth(str, fontOption);
		if(x + len > width) {
			if(!type.canSplit) {
				x = len;
				y += size;// + spacing;
				plotter.iText(0, y, pref + str, size);
			} else {
				double acc = 0.0;
				int i = 0;
				for(; i < str.length() && x + acc < width; ++i) {
					acc += font.getCharWidth(str.charAt(i), fontOption);
				}
				if(i < str.length() && Fragmentor.isPunct(str.charAt(i))) {
					// Grab the punctuation mark
					++i;
				}
				plotter.iText(x, y, pref + str.substring(0, i), size);
				x = 0;
				y += size; //+ spacing;
				if(i < str.length())
					procFragment(type, pref, str.substring(i), size);
			}
		} else {
			plotter.iText(x, y, pref + str, size);
			x += len;
		}
	}

	private void debug(Object msg) {
		 // LambdaLib.log.info("[ACR] " + msg);
	}

	private char read() throws IOException {
		int x = reader.read();
		if(x == -1) {
			eof = true;
		}
		current = (char) x;
		return current;
	}

	private char peekChar(PushbackReader rdr) throws IOException {
		char c = (char) rdr.read();
		rdr.unread(c);
		return c;
	}

	private boolean isEOF(PushbackReader rdr) throws IOException {
		return peekChar(rdr) == (char) -1;
	}

	private char peekChar() throws IOException {
		return peekChar(reader);
	}

	private void require(boolean expr) {
		if(!expr) fail("Assertion error");
	}

	private void require(boolean expr, Object errmsg) {
		if(!expr) fail(errmsg);
	}

	private void fail(Object errmsg) {
		throw new RuntimeException("[ArticleCompiler] " + errmsg);
	}



}
