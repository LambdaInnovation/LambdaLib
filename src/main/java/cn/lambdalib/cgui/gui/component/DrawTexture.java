/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under  
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.lambdalib.cgui.gui.component;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL20.glUseProgram;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.helper.Color;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 */
public class DrawTexture extends Component {
	
	static final ResourceLocation MISSING = new ResourceLocation("liutils:textures/cgui/missing.png");
	
	public ResourceLocation texture = MISSING;
	
	public Color color = new Color(1, 1, 1, 1);
	
	public double zLevel = 0;
	
	public boolean writeDepth = true;
	
	private int shaderId = 0;

	public DrawTexture() {
		super("DrawTexture");
		listen(FrameEvent.class, (w, e) -> 
		{
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			glDisable(GL_ALPHA_TEST);
			glDepthMask(writeDepth);
			glUseProgram(shaderId);
			color.bind();
			double preLevel = HudUtils.zLevel;
			HudUtils.zLevel = zLevel;
			
			if(texture != null && !texture.getResourcePath().equals("<null>")) {
				RenderUtils.loadTexture(texture);
				HudUtils.rect(0, 0, w.transform.width, w.transform.height);
			} else {
				HudUtils.colorRect(0, 0, w.transform.width, w.transform.height);
			}
			HudUtils.zLevel = preLevel;
			glUseProgram(0);
			glDepthMask(true);
		});
	}
	
	public void setShaderId(int id) {
		shaderId = id;
	}
	
	public DrawTexture setTex(ResourceLocation t) {
		texture = t;
		return this;
	}
	
	public DrawTexture setColor4i(int r, int g, int b, int a) {
		color.setColor4d(r / 255.0, g / 255.0, b / 255.0, a / 255.0);
		return this;
	}
	
	public DrawTexture setColor4d(double _r, double _g, double _b, double _a) {
		color.setColor4d(_r, _g, _b, _a);
		return this;
	}
	
	public static DrawTexture get(Widget w) {
		return w.getComponent("DrawTexture");
	}

}
