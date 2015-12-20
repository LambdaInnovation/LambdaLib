package cn.lambdalib.util.client.font;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper to 'fragmentize' string to draw in multi-line in a sane way.
 * @author WeAthFolD
 */
public class Fragmentor {

	private static final char puncts[] = { ',', '.', ':', '，', '。', '、', '；', '：' };

	public interface IFontSizeProvider {
		/**
		 * Get the width of given character when drawed.
		 */
		double getCharWidth(int chr);

		/**
		 * Get the text width that will be drawn.
		 */
		double getTextWidth(String str);
	}

	/**
	 * Converts a string with linesep to a list of string, based on the display property of the given font.
	 */
	public static List<String> toMultiline(String str, IFontSizeProvider font, double limit) {
		Fragmentor frag = new Fragmentor(str);
		List<String> ret = new ArrayList<>();

		StringBuilder builder = new StringBuilder();
		double local_x = 0;
		while (frag.hasNext()) {
			Pair<TokenType, String> next = frag.next();
			TokenType type = next.getLeft();
			String content = next.getRight();
			double len = font.getTextWidth(content);
			if (local_x + len > limit) {
				if (type == TokenType.THISLINE) { // Draws as whole in next line
					if (builder.length() > 0) {
						ret.add(builder.toString());
					}
					builder.setLength(0);
					builder.append(content);
					local_x = len;
				} else { // Seperate to this line and next line
					while (!content.isEmpty()) {
						double acc = 0.0;
						int i = 0;
						for (; i < content.length() && local_x + acc <= limit; ++i) {
							acc += font.getCharWidth(content.charAt(i));
						}

						if (i < content.length() && isPunct(content.charAt(i))) {
							++i;
						}

						ret.add(builder.append(content.substring(0, i)).toString());

						builder.setLength(0);
						local_x = 0;

						content = content.substring(i);
					}
				}
			} else {
				builder.append(content);
				local_x += len;
			}
		}

		if (builder.length() > 0) {
			ret.add(builder.toString());
		}

		return ret;
	}

	public static boolean isPunct(char ch) {
		for(char c : puncts)
			if(c == ch)
				return true;
		return false;
	}

	public enum TokenType { NULL, NEXTLINE, THISLINE }

	final String str;
	int index = 0;

	public Fragmentor(String _str) {
		str = _str;
	}

	public Pair<TokenType, String> next() {
		if(index == str.length()) {
			return Pair.of(TokenType.NULL, null);
		} else {
			TokenType init = getType(str.charAt(index));
			int lindex = index;
			for(++index; index < str.length(); ++index) {
				if(getType(str.charAt(index)) != init)
					break;
			}
			return Pair.of(init, str.substring(lindex, index));
		}
	}

	public boolean hasNext() {
		return index < str.length();
	}

	TokenType getType(char ch) {
		return (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || (ch == '_' || ch == '-') ||
				Character.isWhitespace(ch) || Character.isDigit(ch)) ? TokenType.THISLINE : TokenType.NEXTLINE;
	}

}
