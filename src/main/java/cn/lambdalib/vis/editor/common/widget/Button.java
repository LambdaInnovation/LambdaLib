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
package cn.lambdalib.vis.editor.common.widget;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.component.DrawTexture;
import cn.lambdalib.cgui.gui.component.Tint;
import cn.lambdalib.util.helper.Color;
import net.minecraft.util.ResourceLocation;

/**
 * Default editor button style with tint. Image passed in should be a FULL WHITE image with transparency.
 * @author WeAthFolD
 */
public class Button extends Widget { 
	
	static final Color
		IDLE = new Color(.7, .7, .7, 1),
		HIGHLT = new Color(.9, .9, .9, 1);
	
	public Button(ResourceLocation loc, double width, double height) {
		this(loc);
		transform.setSize(width, height);
	}
	
	public Button(ResourceLocation loc) {
		DrawTexture dt = new DrawTexture();
		dt.setTex(loc);
		
		Tint tint = new Tint();
		tint.affectTexture = true;
		tint.idleColor = IDLE;
		tint.hoverColor = HIGHLT;
		
		addComponent(dt);
		addComponent(tint);
	}
	
	
}
