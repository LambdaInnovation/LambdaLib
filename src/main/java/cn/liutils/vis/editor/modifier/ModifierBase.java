package cn.liutils.vis.editor.modifier;

import java.lang.reflect.Field;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.TextBox;
import cn.liutils.cgui.gui.event.ChangeContentEvent;
import cn.liutils.cgui.gui.event.ConfirmInputEvent;
import cn.liutils.cgui.gui.event.ChangeContentEvent.ChangeContentHandler;
import cn.liutils.cgui.gui.event.ConfirmInputEvent.ConfirmInputHandler;
import cn.liutils.cgui.gui.event.LostFocusEvent;
import cn.liutils.cgui.gui.event.LostFocusEvent.LostFocusHandler;
import cn.liutils.core.LIUtils;
import cn.liutils.vis.editor.common.VEVars;

public abstract class ModifierBase extends Widget {
	
	protected final Field field;
	protected final Object instance;
	
	protected DrawTexture drawer;
	protected TextBox text;
	
	public ModifierBase(Field _field, Object _instance) {
		field = _field;
		instance = _instance;
		
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
	
	protected String repr() throws Exception {
		return field.get(instance).toString();
	}
	
	protected abstract void setValue(String content) throws Exception;

}
