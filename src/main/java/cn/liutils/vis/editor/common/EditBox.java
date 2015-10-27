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
package cn.liutils.vis.editor.common;

import java.lang.reflect.Field;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.TextBox;
import cn.liutils.cgui.gui.event.ChangeContentEvent;
import cn.liutils.cgui.gui.event.ConfirmInputEvent;
import cn.liutils.cgui.gui.event.LostFocusEvent;
import cn.liutils.cgui.gui.event.ChangeContentEvent.ChangeContentHandler;
import cn.liutils.cgui.gui.event.ConfirmInputEvent.ConfirmInputHandler;
import cn.liutils.cgui.gui.event.LostFocusEvent.LostFocusHandler;
import cn.liutils.core.LIUtils;

/**
 * @author WeAthFolD
 */
public abstract class EditBox extends Widget {
	
	protected DrawTexture drawer;
	protected TextBox text;
	
	public EditBox() {
		drawer = new DrawTexture();
		drawer.texture = null;
		drawer.color = VEVars.C_WINDOW_BODY2;
		addComponent(drawer);
		
		transform.setSize(35, 10);
		
		text = new TextBox();
		text.allowEdit = true;
		text.setSize(9);
		addComponent(text);
		
		regEventHandler(new LostFocusHandler() {

			@Override
			public void handleEvent(Widget w, LostFocusEvent event) {
				Widget parent = w.getWidgetParent();
				if(parent != null)
					parent.postEvent(event);
			}
			
		});
		
		regEventHandler(new ChangeContentHandler() {
			@Override
			public void handleEvent(Widget w, ChangeContentEvent event) {
				drawer.color = VEVars.C_MODIFIED;
			}
		});
		
		regEventHandler(new ConfirmInputHandler() {
			@Override
			public void handleEvent(Widget w, ConfirmInputEvent event) {
				try {
					setValue(text.content);
					drawer.color = VEVars.C_WINDOW_BODY2;
					updateRepr();
				} catch(NumberFormatException e) {
					drawer.color = VEVars.C_ERRORED;
				} catch(Exception e) {
					drawer.color = VEVars.C_ERRORED;
					LIUtils.log.error("ModifierBase.confirmInput()", e);
				}
			}
		});
	}
	
	@Override
	public void onAdded() {
		updateRepr();
	}
	
	private void updateRepr() {
		try {
			text.setContent(repr());
		} catch(Exception e) {
			LIUtils.log.error("ModifierBase.onAdded()", e);
			text.setContent("<error>");
		}
	}
	
	protected abstract String repr() throws Exception;
	
	protected abstract void setValue(String content) throws Exception;

}
