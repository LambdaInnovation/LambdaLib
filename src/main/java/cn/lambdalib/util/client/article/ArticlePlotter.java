package cn.lambdalib.util.client.article;

import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.client.font.IFont;
import cn.lambdalib.util.client.font.IFont.FontOption;
import cn.lambdalib.util.key.KeyManager;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ArticlePlotter {

	private List<Instruction> instructions = new ArrayList<>();
	private double maxHeight = 0.0;
	private IKeyResolver resolver = null;

	IFont font;
	FontOption option;

	private interface Instruction {
		void invoke();
	}

	public interface IKeyResolver {
		/**
		 * Resolves the given keyid to the key binding
 		 */
		int resolve(String keyid);
	}

	ArticlePlotter() {}

	public void setKeyResolver(IKeyResolver _resolver) {
		resolver = _resolver;
	}

	// Compile-time instructions

	/**
	 * Instructs to draw a text.
	 */
	public void iText(double x, double y, String str, double size) {
		debug(String.format("%s (%.2f,%.2f)-%.2f", str, x, y, size));
		updht(y + size);
		instructions.add(() -> {
			option.fontSize = size;
			font.draw(str, x, y, option);
		});
	}

	/**
	 * Instructs to draw an image.
	 */
	public void iImage(ResourceLocation image, double x, double y, double width, double height) {
		debug("iImage " + image + " " + x + ", " + y + "(" + width + "," + height + ")");
		updht(y + height);
		instructions.add(() -> {
			RenderUtils.loadTexture(image);
			HudUtils.rect(x, y, width, height);
		});
	}

	/**
	 * Instructs to draw a key description. At the time the resolver of the ArticlePlotter must be set.
	 * @return the width taken to draw this key
	 */
	public double iKey(String key, double x, double y, double scale) {
		debug("iKey " + key + " " + x + "," + y + "," + scale);
		instructions.add(() -> {
			String desc = KeyManager.getKeyName(resolver.resolve(key)); // nullptr if not set the resolver, expected

		});
		return 0;
	}

	private void updht(double ht) {
		if(maxHeight < ht) maxHeight = ht;
	}

	public double getMaxHeight() {
		return maxHeight;
	}

	private void debug(Object msg) {
		// LambdaLib.log.info("[AP] " + msg);
	}

	public void draw() {
		instructions.stream().forEach(x -> x.invoke());
	}

}
