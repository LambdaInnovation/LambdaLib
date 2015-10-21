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
package cn.liutils.vis.editor.common.widget;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.Tint;
import cn.liutils.cgui.gui.event.FrameEvent;
import cn.liutils.cgui.gui.event.MouseDownEvent;
import cn.liutils.util.helper.Font;
import cn.liutils.util.helper.Font.Align;
import cn.liutils.vis.editor.common.VEVars;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 */
public class Toolbar extends Window {

	public interface ICallback { void invoke(); }
	
	private static final double SZ = 10;
	private int buttonCount = 0;
	
	public Toolbar() {
		super("Toolbar");
	}
	
	public void addButton(String name, ICallback action) {
		addButton(name, name, action);
	}
	
	public void addButton(String tex, String name, ICallback action) {
		addButton(VEVars.tex("toolbar/" + tex), name, action);
	}
	
	public void addButton(ResourceLocation texture, String name, ICallback action) {
		Widget b = new Widget();
		
		b.transform.setSize(SZ, SZ).setPos(4 + buttonCount * (SZ + 5), 2);
		
		DrawTexture dt = new DrawTexture();
		dt.texture = texture;
		
		Tint tint = new Tint();
		tint.affectTexture = true;
		tint.idleColor.setColor4d(1, 1, 1, 0.7);
		tint.hoverColor.setColor4d(1, 1, 1, 1);
		
		b.addComponents(dt, tint);
		
		b.regEventHandler(FrameEvent.class, (widget, e) -> {
			if(e.hovering) {
				Font.font.draw(name, 0, 15, 10, 0xffffff, Align.CENTER);
			}
		});
		
		b.regEventHandler(MouseDownEvent.class, (widget, e) -> {
			action.invoke();
		});
		
		body.addWidget(b);
		++buttonCount;
		
		transform.setSize(Math.max(60, 8 + buttonCount * (SZ + 5)), 26);
	}

}
