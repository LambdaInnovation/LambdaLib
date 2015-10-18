package cn.liutils.vis.editor.common;

import cn.liutils.util.helper.Color;
import net.minecraft.util.ResourceLocation;

/**
 * Globally used constants for VisEditor.
 */
public class VEVars {
	
	public static final Color
		C_WINDOW_TOP = new Color(0xff1e1e1e),
		C_WINDOW_BODY = new Color(0xff292929),
		C_WINDOW_FOREGROUND = new Color(0xff474747),
		C_WINDOW_HIGHLIGHT = new Color(0xff454545),
		C_WINDOW_HIGHLIGHT2 = new Color(0xffb1b1b1),
		C_WINDOW_TEXT = new Color(0xffa8a8a8),
		C_MODIFIED = new Color(0xff514430),
		C_ERRORED = new Color(0xff4f271c);
	
	public static ResourceLocation tex(String loc) {
		return new ResourceLocation("liutils:textures/vis/" + loc + ".png");
	}
	
}
