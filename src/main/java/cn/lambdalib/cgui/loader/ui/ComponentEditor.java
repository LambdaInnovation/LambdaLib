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
package cn.lambdalib.cgui.loader.ui;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import cn.lambdalib.cgui.client.CGUILang;
import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.annotations.EditIgnore;
import cn.lambdalib.cgui.gui.component.Component;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.helper.Color;
import cn.lambdalib.util.helper.Font;
import net.minecraft.util.ResourceLocation;

/**
 * Editor for a single property type. Currently is generated each time it is queried.
 * @author WeAthFolD
 */
public class ComponentEditor extends Window {
	
	static Map<Class, Class<? extends ElementEditor>> editors = new HashMap();
	static {
		Class[] arr = { Integer.TYPE, Integer.class, String.class, 
			Double.TYPE, Double.class, Float.TYPE, Float.class, ResourceLocation.class
		};
		for(Class c : arr) {
			editors.put(c, ElementEditor.InputBox.class);
		}
		
		editors.put(Color.class, ElementEditor.ColorBox.class);
		editors.put(Boolean.TYPE, ElementEditor.CheckBox.class);
		editors.put(Boolean.class, ElementEditor.CheckBox.class);
		editors.put(Enum.class, ElementEditor.EnumSelector.class);
	}
	
	Widget widget;
	Component target;
	
	public ComponentEditor(GuiEdit guiEdit, Widget _widget, Component _target) {
		super(guiEdit, CGUILang.guiComeditor() + _target.name, true);
		widget = _widget;
		target = _target;
		generate();
		transform.x = 100;
		transform.y = 20;
		transform.width = 125;
	}
	
	private void generate() {
		try {
			double y = 12;
			
			for(final Field f : target.getPropertyList()) {
				//Generation~
				ElementEditor ee = getElementEditor(f);
				if(ee == null) {
					LambdaLib.log.error("Can't find element editor for type " + f.getType());
					continue;
				}
				if(f.isAnnotationPresent(EditIgnore.class)) {
					continue;
				}
				
				Widget drawer = new Widget();
				drawer.transform.x = 2;
				drawer.transform.y = y;
				drawer.listen(FrameEvent.class, (w, e) -> {
					Font.font.draw(f.getName(), 0, 0, 9, 0xffffff);
				});
				addWidget(drawer);
				
				/**
				 * Inject instance
				 */
				ee.editor = this;
				ee.transform.y = y + 10 + ee.transform.y;
				y += 10 + ee.transform.height;
				
				addWidget(ee);
			}
			
			transform.height = y + 5;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private ElementEditor getElementEditor(Field f) {
		Class c = f.getType();
		Class handler = null;
		while(c != null && handler == null) {
			handler = editors.get(c);
			c = c.getSuperclass();
		}
		if(handler == null) return null;
		try {
			return (ElementEditor) handler.getConstructor(Field.class).newInstance(f);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
