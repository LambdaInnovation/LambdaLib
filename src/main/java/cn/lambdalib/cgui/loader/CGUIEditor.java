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
package cn.lambdalib.cgui.loader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.component.Component;
import cn.lambdalib.cgui.gui.component.Draggable;
import cn.lambdalib.cgui.gui.component.DrawTexture;
import cn.lambdalib.cgui.gui.component.Outline;
import cn.lambdalib.cgui.gui.component.ProgressBar;
import cn.lambdalib.cgui.gui.component.TextBox;
import cn.lambdalib.cgui.gui.component.Tint;
import cn.lambdalib.cgui.gui.component.Transform;
import cn.lambdalib.cgui.gui.component.VerticalDragBar;

import java.util.Set;

/**
 * Main loading interface to provide templates&customized objects to CGUI.
 * Only Components and Widget templates registered in this class are available
 * for in-editor usage.
 * @author WeAthFolD
 */
public class CGUIEditor {
	
	static Map<String, Component> components = new HashMap();
	static Map<String, Widget> templates = new HashMap();
	
	//Built-ins.
	static {
		//Default Properties
		addComponent(new Transform());
		addComponent(new TextBox());
		addComponent(new Draggable());
		addComponent(new DrawTexture());
		addComponent(new Tint());
		addComponent(new ProgressBar());
		addComponent(new VerticalDragBar());
		addComponent(new Outline());
		//addComponent(new ElementList());
		
		//Default templates
		{ //"default"
			Widget def = new Widget().addComponent(new DrawTexture());
			addTemplate("Default", def);
		}
		
		{ //"input_box"
			Widget inp = new Widget().addComponent(new TextBox());
			addTemplate("TextBox", inp);
		}
	}
	
	//Template
	public static void addTemplate(String str, Widget template) {
		templates.put(str, template);
	}
	
	public static Set<Entry<String, Widget>> getTemplates() {
		return templates.entrySet();
	}
	
	public static Widget createFromTemplate(String name) {
		Widget prototype = templates.get(name);
		return prototype == null ? null : prototype.copy();
	}
	
	//Component
	public static void addComponent(Component c) {
		components.put(c.name, c);
	}
	
	public static Collection<Component> getComponents() {
		return components.values();
	}

}
