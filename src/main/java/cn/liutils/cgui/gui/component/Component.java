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
package cn.liutils.cgui.gui.component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.annotations.CopyIgnore;
import cn.liutils.cgui.gui.annotations.EditIgnore;
import cn.liutils.cgui.gui.event.GuiEvent;
import cn.liutils.cgui.gui.event.GuiEventBus;
import cn.liutils.cgui.gui.event.IGuiEventHandler;
import cn.liutils.core.LIUtils;
import cn.liutils.util.helper.TypeHelper;

/**
 * Component is the basic content of Widget. It can define a set of EventHandlers and store information itself.
 * The (non-runtime-state) information stored in the component will be copied as a widget is copied.
 * @author WeAthFolD
 */
public class Component {
	
	static Map<Class, List<Field>> copiedFields = new HashMap();
	
	public final String name;
	
	public boolean enabled = true;
	
	@EditIgnore
	public boolean canEdit = true;
	
	/**
	 * This SHOULD NOT be edited after creation, represents the widget instance this component is in.
	 */
	@EditIgnore
	@CopyIgnore
	public Widget widget;
	
	private class Node {
		Class<? extends GuiEvent> type;
		IGuiEventHandler handler;
		int prio;
	}
	
	private List<Node> addedHandlers = new ArrayList();
	
	public Component(String _name) {
		name = _name;
		checkCopyFields();
	}

	private List<Field> checkCopyFields() {
		if(copiedFields.containsKey(getClass()))
			return copiedFields.get(getClass());
		List<Field> ret = new ArrayList<Field>();
		for(Field f : getClass().getFields()) {
			if(((f.getModifiers() & Modifier.FINAL) == 0)
			&& !f.isAnnotationPresent(CopyIgnore.class) && TypeHelper.isTypeSupported(f.getType())) {
				ret.add(f);
			}
		}
		copiedFields.put(getClass(), ret);
		return ret;
	}
	
	protected <T extends GuiEvent> void addEventHandler(Class<? extends T> type, IGuiEventHandler<T> handler) {
		addEventHandler(type, handler, 0);
	}
	
	protected <T extends GuiEvent> void addEventHandler(Class<? extends T> type, IGuiEventHandler<T> handler, int prio) {
		if(widget != null)
			throw new RuntimeException("Can only add event handlers before componenet is added into widget");
		Node n = new Node();
		n.type = type;
		n.handler = handler;
		n.prio = prio;
		addedHandlers.add(n);
	}
	
	/**
	 * Called when the component is added into a widget, and the widget field is correctly set.
	 */
	public void onAdded() {
		for(Node n : addedHandlers) {
			widget.listen(n.type, n.handler, n.prio);
		}
	}
	
	public void onRemoved() {
		for(Node n : addedHandlers) {
			widget.unlisten(n.type, n.handler);
		}
	}
	
	public Component copy() {
		try {
			Component c = getClass().newInstance();
			for(Field f : copiedFields.get(getClass())) {
				TypeHelper.set(f, c, TypeHelper.copy(f, this));
			}
			return c;
		} catch(Exception e) {
			LIUtils.log.error("Unexpected error occured copying component of type " + getClass());
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean canStore() {
		return true;
	}
	
	/**
	 * Recover all the data fields within the component with the data map specified.
	 */
	public void fromPropertyMap(Map<String, String> map) {
		List<Field> fields = checkCopyFields();
		for(Field f : fields) {
			String val = map.get(f.getName());
			if(val != null) {
				TypeHelper.edit(f, this, val);
			}
		}
	}
	
	public Map<String, String> getPropertyMap() {
		Map<String, String> ret = new HashMap();
		for(Field f : checkCopyFields()) {
			String val = TypeHelper.repr(f, this);
			if(val != null) {
				ret.put(f.getName(), val);
			}
		}
		
		return ret;
	}
	
	public Collection<Field> getPropertyList() {
		return copiedFields.get(getClass());
	}
	
}

