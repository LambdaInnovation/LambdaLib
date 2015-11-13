package cn.lambdalib.util.client.article;

import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.helper.Font;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by WeAth on 2015/11/13.
 */
public class ArticlePlotter {

	private List<Instruction> instructions = new ArrayList<>();
	private double maxHeight = 0.0;

	private interface Instruction {
		void invoke();
	}

	// Compile-time instructions
	public void iText(double x, double y, String str, double size) {
		debug(String.format("%s (%.2f,%.2f)-%.2f", str, x, y, size));
		updht(y + size);
		instructions.add(() -> {
			Font.font.draw(str, x, y, size, 0xffffff);
		});
	}

	public void iImage(ResourceLocation image, double x, double y, double width, double height) {
		debug("iImage " + image + " " + x + ", " + y + "(" + width + "," + height + ")");
		updht(y + height);
		instructions.add(() -> {
			RenderUtils.loadTexture(image);
			HudUtils.rect(x, y, width, height);
		});
	}

	public void iKey(String key, double x, double y, double scale) {
		debug("iKey " + key + " " + x + "," + y + "," + scale);
		instructions.add(() -> {
			// TODO
		});
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
