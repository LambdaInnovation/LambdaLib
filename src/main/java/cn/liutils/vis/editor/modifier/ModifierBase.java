package cn.liutils.vis.editor.modifier;

import java.lang.reflect.Field;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.TextBox;
import cn.liutils.cgui.gui.event.ChangeContentEvent;
import cn.liutils.cgui.gui.event.ChangeContentEvent.ChangeContentHandler;
import cn.liutils.cgui.gui.event.ConfirmInputEvent;
import cn.liutils.cgui.gui.event.ConfirmInputEvent.ConfirmInputHandler;
import cn.liutils.cgui.gui.event.LostFocusEvent;
import cn.liutils.cgui.gui.event.LostFocusEvent.LostFocusHandler;
import cn.liutils.core.LIUtils;
import cn.liutils.vis.editor.common.EditBox;
import cn.liutils.vis.editor.common.VEVars;

public abstract class ModifierBase extends EditBox {
	
	protected final Field field;
	protected final Object instance;
	
	public ModifierBase(Field _field, Object _instance) {
		field = _field;
		instance = _instance;
	}
	
	protected String repr() throws Exception {
		return field.get(instance).toString();
	}
	
	protected abstract void setValue(String content) throws Exception;

}
