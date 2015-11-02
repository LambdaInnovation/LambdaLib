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
package cn.liutils.cgui.loader.ui;

import cn.liutils.cgui.client.CGUILang;
import cn.liutils.cgui.gui.LIGui;
import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.WidgetContainer;
import cn.liutils.cgui.gui.event.DragEvent;
import cn.liutils.cgui.gui.event.FrameEvent;
import cn.liutils.cgui.gui.event.GainFocusEvent;
import cn.liutils.cgui.gui.event.global.AddWidgetEvent;
import cn.liutils.cgui.loader.ui.event.AddTargetEvent;
import cn.liutils.util.client.HudUtils;
import cn.liutils.util.helper.Color;
import cn.liutils.util.helper.Font;

/**
 * Handler of edit content also provides environment information for GuiEdit and other toolbars.
 * @author WeAthFolD
 */
public class LIGuiPlayground extends LIGui {
	
	final GuiEdit guiEdit;
	
	public LIGuiPlayground(GuiEdit _guiEdit) {
		guiEdit = _guiEdit;
		
		eventBus.reg(AddWidgetEvent.class, (w, e) -> {
			guiEdit.getGui().postEvent(new AddTargetEvent(w));
		});
	}
	
	@Override
	public void draw(double mx, double my) {
		super.draw(mx, my);
		
		String focusName = this.getFocus() == null ? "<" + CGUILang.txtBackground() + ">" : 
			this.getWidgetName(getFocus());
		Font.font.draw(CGUILang.txtSelection() + focusName, 5, 5, 10, 0x89b1e7);
	}
	
	@Override
	public void onWidgetAdded(String name, Widget w) {
		super.onWidgetAdded(name, w);
		injectEvents(w);
	}
	
	private void injectEvents(Widget w) {
		//Add selection indicator
		final Color c = new Color(112, 223, 122, 200);
		w.regEventHandler(FrameEvent.class, (ww, e) -> {
			if(getFocus() == w) {
				c.bind();
				HudUtils.drawRectOutline(0, 0, w.transform.width, w.transform.height, 1);
			} else {
				//HudUtils.drawRectOutline(0, 0, w.transform.width, w.transform.height, 1);
			}
		});
		w.regEventHandler(DragEvent.class, (ww, e) -> {
			if(w.isFocused()) {
				w.getGui().updateDragWidget();
			}
		});
		w.regEventHandler(GainFocusEvent.class, (ww, e) -> {
			new SelectedWidgetBar(guiEdit, w);
		});
		
		for(Widget w2 : w.getDrawList()) {
			injectEvents(w2);
		}
	}
	
	/**
	 * DIRRRRRTY!
	 */
	@Override
	protected Widget gtnTraverse(double x, double y, Widget node, WidgetContainer set) {
		Widget res = null;
		boolean sub = node == null || (node.transform.doesDraw && node.transform.doesListenKey);
		if(sub && node != null && node.isPointWithin(x, y) && node.isFocused()) {
			res = node;
		}
		
		if(!sub) return res;
		
		Widget next = null;
		for(Widget wn : set) {
			Widget tmp = gtnTraverse(x, y, wn, wn);
			if(tmp != null)
				next = tmp;
		}
		return next == null ? res : next;
	}
}
