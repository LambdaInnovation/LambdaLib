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
package cn.liutils.vis.editor.modifier;

import cn.liutils.cgui.gui.LIGui;
import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.TextBox;
import cn.liutils.cgui.gui.component.Transform.HeightAlign;
import cn.liutils.cgui.gui.event.LostFocusEvent;
import cn.liutils.cgui.gui.event.LostFocusEvent.LostFocusHandler;
import cn.liutils.vis.editor.common.VEVars;

/**
 * @author WeAthFolD
 */
public class BoxBase extends Widget {
	
	public BoxBase() {
		DrawTexture dt = new DrawTexture();
		dt.color = VEVars.C_WINDOW_BODY;
		dt.texture = null;
		addComponent(dt);
	}
	
	protected Widget text(String content, double x, double y) {
		Widget ret = new Widget();
		ret.transform.x = x;
		ret.transform.y = y;
		ret.transform.setSize(0, 20);
		
		TextBox text = new TextBox();
		text.heightAlign = HeightAlign.TOP;
		text.content = content;
		text.size = 10;
		ret.addComponent(text);
		
		addWidget(ret);
		return ret;
	}
	
	@Override
	public void onAdded() {
		LIGui gui = getGui();
		gui.gainFocus(this);
		
		this.regEventHandler(new LostFocusHandler() {
			@Override
			public void handleEvent(Widget w, LostFocusEvent event) {
				System.out.println("NF " + event.newFocus);
				if(event.newFocus == null || !event.newFocus.isChildOf(BoxBase.this))
					dispose();
			}
		});
	}
	
}
