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
package cn.liutils.vis.editor.common.widget;

import java.util.ArrayList;
import java.util.List;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.ElementList;
import cn.liutils.cgui.gui.component.TextBox;
import cn.liutils.cgui.gui.component.Tint;
import cn.liutils.cgui.gui.component.Transform.HeightAlign;
import cn.liutils.cgui.gui.event.MouseDownEvent;
import cn.liutils.vis.editor.common.IHierarchy;
import cn.liutils.vis.editor.common.VEVars;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 */
public class WindowHierarchy extends Window implements IHierarchy {
	
	public static final double
		WIDTH = 100,
		HEIGHT = 230,
		ELEMENT_HT = 12,
		ELEMENT_ICON_SZ = 10;
	
	private ElementList list;
	
	private List<Element> elements = new ArrayList();

	public WindowHierarchy() {
		super("Hierarchy");
		initTopButton(TopButtonType.MINIMIZE);
		transform.setSize(WIDTH, HEIGHT);
	}
	
	@Override
	public void onAdded() {
		rebuild();
	}
	
	public void addElement(Element e) {
		elements.add(e);
	}
	
	/**
	 * Rebuild the display list.
	 */
	public void rebuild() {
		if(list != null)
			body.removeComponent(list);
		
		list = new ElementList();
		for(Element e : elements) {
			e.window = this;
			list.addWidget(e);
			e.onRebuild(list);
		}
		body.addComponent(list);
	}
	
	public static abstract class Element extends Widget {
		
		int indent;
		String name;
		ResourceLocation icon;
		
		WindowHierarchy window;
		
		protected Widget iconArea, textArea;
		
		boolean init = false;
		
		public Element(String _name, ResourceLocation _icon) {
			name = _name;
			icon = _icon;
			transform.setSize(WIDTH, ELEMENT_HT);
		}
		
		public void addAdditional(Widget w) {
			hierarchy().body.addWidget(w);
			this.placeAround(w);
		}
		
		public void placeAround(Widget widget) {
			getGui().moveWidgetToAbsPos(widget,
					this.x + this.scale * this.transform.width,
					this.y);
		}
		
		@Override
		public void onAdded() {
			if(!init) {
				init = true;
				double indentOffset = indent * 3;
				
				{
					iconArea = new Widget();
					iconArea.transform.setPos(5 + indentOffset, 2).setSize(ELEMENT_ICON_SZ, ELEMENT_ICON_SZ);
					iconArea.transform.doesListenKey = false;
					DrawTexture dt = new DrawTexture();
					dt.texture = icon;
					iconArea.addComponent(dt);
					addWidget(iconArea);
				}
				{
					textArea = new Widget();
					textArea.transform.setPos(18 + indentOffset, 0).setSize(0, ELEMENT_HT);
					textArea.transform.doesListenKey = false;
					
					TextBox box = new TextBox();
					box.content = name;
					box.heightAlign = HeightAlign.BOTTOM;
					box.size = 9;
					textArea.addComponent(box);
					
					addWidget(textArea);
				}
				
				addComponent(new Tint());
				
				listen(MouseDownEvent.class, (w, e) -> { onClick(); });
			}
		}
		
		/**
		 * The name of this element to display.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Basically any element will have some behaviour upon clicking. 
		 *  So a event handler is hard-coded and delegated to the method to avoid code overhead.
		 */
		public abstract void onClick();
		
		/**
		 * Callback when the display element list is being rebuilt.
		 *  If this element has any child, it should 
		 */
		public void onRebuild(ElementList list) {}
		
		/**
		 * Available AFTER the widget is added. Else return null.
		 */
		protected WindowHierarchy hierarchy() {
			return window;
		}
		
	}
	
	public static class Folder extends Element implements IHierarchy {
		
		static final ResourceLocation
			OPEN = VEVars.tex("hierarchy/folder_open"),
			CLOSE = VEVars.tex("hierarchy/folder_close");
		
		public ResourceLocation
			iconOpen = OPEN,
			iconClose = CLOSE;
		
		public Folder(String _name, ResourceLocation _icon) {
			super(_name, _icon);
			iconOpen = iconClose = _icon;
		}
		
		public Folder(String _name) {
			this(_name, CLOSE);
		}

		private List<Element> elements = new ArrayList();
		public boolean open;
		
		public void addElement(Element e) {
			elements.add(e);
			e.indent = this.indent + 1;
		}

		@Override
		public void onClick() {
			open = !open;
			
			WindowHierarchy hier = hierarchy();
			DrawTexture.get(iconArea).texture = open ? iconOpen : iconClose;
			hier.rebuild();
		}
		
		@Override
		public void onRebuild(ElementList list) {
			if(!open)
				return;
			WindowHierarchy hier = hierarchy();
			for(Element e : elements) {
				e.window = hier;
				e.indent = this.indent + 1;
				list.addWidget(e);
				e.onRebuild(list);
			}
		}
		
	}

}
