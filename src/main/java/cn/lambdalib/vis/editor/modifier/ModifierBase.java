package cn.lambdalib.vis.editor.modifier;

import java.lang.reflect.Field;

import cn.lambdalib.vis.editor.common.EditBox;

public abstract class ModifierBase extends EditBox {
	
	protected final Field field;
	protected final Object instance;
	
	public ModifierBase(Field _field, Object _instance) {
		field = _field;
		instance = _instance;
		
		field.setAccessible(true);
	}
	
	protected String repr() throws Exception {
		return field.get(instance).toString();
	}
	
	protected abstract void setValue(String content) throws Exception;

}
