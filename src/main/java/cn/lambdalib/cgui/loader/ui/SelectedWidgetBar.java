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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.component.Component;
import cn.lambdalib.cgui.gui.component.DrawTexture;
import cn.lambdalib.cgui.gui.component.Tint;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.cgui.gui.event.LostFocusEvent;
import cn.lambdalib.cgui.gui.event.LeftClickEvent;
import cn.lambdalib.cgui.loader.CGUIEditor;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.helper.Font;
import cn.lambdalib.util.helper.Font.Align;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 *
 */
public class SelectedWidgetBar extends Window {
	
	static final ResourceLocation TEX_REMOVE = GuiEdit.tex("remove");
	
	final Widget target;
	
	static final double HT = 10;
	
	final GuiEdit guiEdit;

	public SelectedWidgetBar(GuiEdit _guiEdit, Widget _target) {
		super(_guiEdit, "Properties", false, new double[] { 220, 20 });
		guiEdit = _guiEdit;
		target = _target;
		transform.width = 100;
		transform.height = HT;
		
		initWidgets();
		initEvents();
		
		guiEdit.changeSelectedEditor(this);
	}
	
	private List<Component> getCanAddList() {
		List<Component> ret = new ArrayList(CGUIEditor.getComponents());
		Iterator<Component> iter = ret.iterator();
		while(iter.hasNext()) {
			Component c = iter.next();
			if(target.getComponent(c.name) != null) {
				iter.remove();
			}
		}
		return ret;
	}
	
	private void initWidgets() {
		addWidget(new ComponentSelection());
	}
	
	private void initEvents() {
		listen(FrameEvent.class, (w, e) -> {
			if(target.disposed || target != guiEdit.toEdit.getFocus()) {
				w.dispose();
			}
		});
	}
	
	private class ComponentSelection extends Window {
		
		Widget curEditor;
		boolean listSpawned = false;
		
		public ComponentSelection() {
			super(SelectedWidgetBar.this.guiEdit, "Components", false);
			transform.x = 0;
			transform.y = HT;
			transform.width = 100;
			transform.height = 24 + target.getComponentList().size() * 11;
			
			int i = 0;
			for(Component prop : target.getComponentList()) {
				if(prop.canEdit)
					addWidget(new ComponentButton(prop, i++));
			}
			//SO SAD
			{
				Widget add = new Widget();
				add.transform.setPos(0, 11 + i * 11);
				add.transform.setSize(100, 10);
				add.addComponent(new Tint());
				ResourceLocation tex = GuiEdit.tex("toolbar/add");
				add.listen(FrameEvent.class, (w, e) -> {
					RenderUtils.loadTexture(tex);
					GL11.glColor4d(1, 1, 1, 1);
					HudUtils.rect(45, 0, 10, 10);
				});
				
				final int y = 11 * (i + 2);
				add.listen(LeftClickEvent.class, (w, e) -> {

					if(listSpawned)
						return;
					listSpawned = true;
					
					//Setup background list
					Widget list = new Widget();
					list.listen(LostFocusEvent.class, (ww, event) -> {
						ww.dispose();
					});
					list.addComponent(new DrawTexture().setTex(null).setColor4d(.8, .8, 1, 0.3));
					Collection<Component> components = CGUIEditor.getComponents();
					list.transform.setSize(80, 10 * components.size());
					list.transform.setPos(10, y);
					
					//Use a loop to setup all the sub-components.
					int j = 0;
					for(final Component c : getCanAddList()) {
						Widget one = new Widget();
						one.transform.y = (j++) * 10;
						one.transform.setSize(80, 10);
						one.addComponent(new Tint());
						one.listen(FrameEvent.class, (ww, event) -> {
							String text = c.name;
							Font.font.draw(text, 40, 1, 8, 0xffffff, Align.CENTER);
						});
						one.listen(LeftClickEvent.class, (ww, ee) -> {
							target.addComponent(c.copy());
							//Rebuild the component selection GUI
							ComponentSelection.this.dispose();
							SelectedWidgetBar.this.addWidget(new ComponentSelection());
						});
						list.addWidget(one);
					}
					
					ComponentSelection.this.addWidget(list);
					//getGui().gainFocus(list);
				});
				addWidget(add);
			}
		}
		
		void setPropertyEditor(Widget ce) {
			if(curEditor != null) {
				curEditor.dispose();
			}
			curEditor = ce;
			ce.transform.y = -10;
			addWidget(curEditor);
		}
		
		private class ComponentButton extends Widget {
			
			final Component c;
			
			public ComponentButton(Component _c, int n) {
				c = _c;
				
				transform.x = 0;
				transform.y = 11 + n * 11;
				transform.width = 100;
				transform.height = 10;
				
				addComponent(new Tint());
				
				listen(LeftClickEvent.class, (w, e) -> {
					setPropertyEditor(new ComponentEditor(guiEdit, target, c));
				});
				
				listen(FrameEvent.class, (w, e) -> {
					Font.font.draw(c.name, 50, 2, 7, 0xffffff, Align.CENTER);
				});
				
				addSubWidgets();
			}
			
			private void addSubWidgets() {
				//Add the remove button, if needed to.
				if(c.name.equals("Transform"))
					return;
				Widget w = new Widget();
				w.transform.setSize(10, 10).setPos(85, 0);
				w.addComponent(new DrawTexture().setTex(TEX_REMOVE));
				w.addComponent(new Tint());
				w.listen(LeftClickEvent.class, (ww, e) -> {
					target.removeComponent(c);
					ComponentSelection.this.dispose();
					SelectedWidgetBar.this.addWidget(new ComponentSelection());
				});
				addWidget(w);
			}
			
		}
		
	}
	
}
