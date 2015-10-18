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

import java.lang.reflect.Field;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.TextBox;
import cn.liutils.cgui.gui.event.ChangeContentEvent;
import cn.liutils.cgui.gui.event.ChangeContentEvent.ChangeContentHandler;
import cn.liutils.cgui.gui.event.ConfirmInputEvent;
import cn.liutils.cgui.gui.event.ConfirmInputEvent.ConfirmInputHandler;
import cn.liutils.core.LIUtils;
import cn.liutils.vis.editor.common.VEVars;

/**
 * @author WeAthFolD
 */
public class IntegerModifier extends Widget {
	
	public final Field field;
	public final Object instance;
	
	private DrawTexture drawer;
	private TextBox text;
	
	public IntegerModifier(Field _field) {
		this(_field, null);
	}
	
	public IntegerModifier(Field _field, Object _instance) {
		field = _field;
		instance = _instance;
		
		transform.setSize(40, 14);
		
		drawer = new DrawTexture();
		drawer.texture = null;
		drawer.color = VEVars.C_WINDOW_BODY;
		addComponent(drawer);
		
		text = new TextBox();
		text.allowEdit = true;
		text.setSize(13);
		addComponent(text);
		
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
					field.set(instance, Integer.valueOf(text.content));
					w.dispose();
				} catch(NumberFormatException e) {
					drawer.color = VEVars.C_ERRORED;
				} catch(Exception e) {
					drawer.color = VEVars.C_ERRORED;
					LIUtils.log.error("IntegerModifier.confirmInput()", e);
				}
			}
		});
	}
	
	@Override
	public void onAdded() {
		text.setContent(String.valueOf(getValue()));
	}
	
	private int getValue() {
		try {
			return field.getInt(instance);
		} catch(Exception e) {
			LIUtils.log.error("IntegerModifier.getValue()", e);
			return -1;
		}
	}
	
	private boolean setValue(int val) {
		try {
			field.setInt(instance, val);
			return true;
		} catch(Exception e) {
			LIUtils.log.error("IntegerModifier.setValue(" + val + ")", e);
			return false;
		}
	}
	
}
