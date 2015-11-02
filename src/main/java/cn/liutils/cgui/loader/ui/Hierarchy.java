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
package cn.liutils.cgui.loader.ui;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import cn.liutils.cgui.client.CGUILang;
import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.ElementList;
import cn.liutils.cgui.gui.component.ElementList.ProgressChangedEvent;
import cn.liutils.cgui.gui.component.TextBox;
import cn.liutils.cgui.gui.component.Tint;
import cn.liutils.cgui.gui.component.VerticalDragBar;
import cn.liutils.cgui.gui.component.VerticalDragBar.DraggedEvent;
import cn.liutils.cgui.gui.event.ChangeContentEvent;
import cn.liutils.cgui.gui.event.ConfirmInputEvent;
import cn.liutils.cgui.gui.event.FrameEvent;
import cn.liutils.cgui.gui.event.GainFocusEvent;
import cn.liutils.cgui.gui.event.MouseDownEvent;
import cn.liutils.cgui.loader.ui.event.AddTargetEvent;
import cn.liutils.util.client.HudUtils;
import cn.liutils.util.helper.Font;
import cn.liutils.util.helper.Font.Align;
import net.minecraft.util.ResourceLocation;


/**
 * @author WeAthFolD
 *
 */
public class Hierarchy extends Window {
	
	Widget hList, dragbar;

	public Hierarchy(GuiEdit _guiEdit) {
		super(_guiEdit, CGUILang.guiHierarchy(), true, new double[] { 150, 80 });
		transform.width = 100;
		transform.height = 120;
	}
	
	@Override
	public void onAdded() {
		super.onAdded();
		regEventHandler(FrameEvent.class, (w, e) -> {
			drawSplitLine(30);
		});
		
		buildHierarchy();
		addButtons();
		getGui().eventBus.reg(AddTargetEvent.class, (w, e) -> {
			buildHierarchy();
		});
	}
	
	Widget getAccessTarget() {
		return guiEdit.toEdit.getFocus();
	}
	
	private void addButtons() {
		Widget tmp;
		
		tmp = setupButton(0, "arrow_left", CGUILang.butDechild());
		tmp.regEventHandler(MouseDownEvent.class, (w, e) -> {
			if(getAccessTarget() != null) {
				getAccessTarget().moveLeft();
				buildHierarchy();
			}
		});
		
		tmp = setupButton(1, "arrow_right", CGUILang.butChild());
		tmp.regEventHandler(MouseDownEvent.class, (w, e) -> {
			if(getAccessTarget() != null) {
				getAccessTarget().moveRight();
				buildHierarchy();
			}
		});
		
		tmp = setupButton(2, "arrow_up", CGUILang.butMoveUp());
		tmp.regEventHandler(MouseDownEvent.class, (w, e) -> {
			if(getAccessTarget() != null) {
				getAccessTarget().moveUp();
				buildHierarchy();
			}
		});
		
		tmp = setupButton(3, "arrow_down", CGUILang.butMoveDown());
		tmp.regEventHandler(MouseDownEvent.class, (w, e) -> {
			if(getAccessTarget() != null) {
				getAccessTarget().moveDown();
				buildHierarchy();
			}
		});
		
//		tmp = setupButton(4, "rename", CGUILang.butRename());
//		tmp.regEventHandler(new MouseDownHandler() {
//			@Override
//			public void handleEvent(Widget w, MouseDownEvent event) {
//				if(getAccessTarget() != null) {
//					TextBox box = handlers.get(getAccessTarget()).box;
//					box.allowEdit = true;
//					handlers.get(getAccessTarget()).transform.doesListenKey = true;
//					System.out.println("Rename callback");
//				}
//			}
//		});

		tmp = setupButton(4, "remove", CGUILang.butRemove());
		tmp.regEventHandler(MouseDownEvent.class, (w, e) -> {
			if(getAccessTarget() != null) {
				getAccessTarget().dispose();
				buildHierarchy();
			}
		});
		
		tmp = setupButton(5, "duplicate", CGUILang.butDuplicate());
		tmp.regEventHandler(MouseDownEvent.class, (w, e) -> {
			if(getAccessTarget() != null) {
				getAccessTarget().getAbstractParent().addWidget(getAccessTarget().copy());
				buildHierarchy();
			}
		});
		
		tmp = setupButton(6, "up", "Move Up");
		tmp.transform.setPos(90, 30);
		tmp.regEventHandler(MouseDownEvent.class, (w, e) -> {
			if(hList != null) {
				ElementList list = ElementList.get(hList);
				list.progressLast();
			}
		});
		
		tmp = setupButton(7, "down", "Move Down");
		tmp.transform.setPos(90, 110);
		tmp.regEventHandler(MouseDownEvent.class, (w, e) -> {
			if(hList != null) {
				ElementList list = ElementList.get(hList);
				list.progressNext();
			}
		});
		
		{
			tmp = new Widget();
			tmp.transform.setSize(10, 10);
			tmp.transform.setPos(90, 40);
			
			final VerticalDragBar bar = new VerticalDragBar();
			bar.setArea(40, 100);
			tmp.addComponent(bar);
			
			DrawTexture dt = new DrawTexture().setTex(null).setColor4i(200, 200, 255, 200);
			tmp.addComponent(dt);
			
			tmp.regEventHandler(DraggedEvent.class, (w, e) -> {
				double p = bar.getProgress();
				if(hList != null) {
					ElementList list = ElementList.get(hList);
					list.setProgress((int) Math.round(p * list.getMaxProgress()));
				}
			});
			
			addWidget(tmp);
			dragbar = tmp;
		}
	}
	
	Map<Widget, SingleWidget> handlers = new HashMap();
	
	private void buildHierarchy() {
		if(hList != null) 
			hList.dispose();
		
		handlers.clear();
		hList = new Widget();
		hList.transform.x = 2;
		hList.transform.y = 32;
		hList.transform.width = 80;
		hList.transform.height = 86;
		
		final ElementList el = new ElementList();
		for(Widget w : guiEdit.toEdit.getDrawList()) {
			if(!w.disposed)
				hierarchyAdd(el, w);
		}
		hList.addComponent(el);
		hList.regEventHandler(ProgressChangedEvent.class, (w, e) -> {
			double p = (double)el.getProgress() / el.getMaxProgress();
			VerticalDragBar.get(dragbar).setProgress(dragbar, p);
		});
		
		
		addWidget(hList);
	}
	
	private void hierarchyAdd(ElementList list, Widget w) {
		SingleWidget sw;
		list.addWidget(sw = new SingleWidget(w));
		handlers.put(w, sw);
		for(Widget ww : w.getDrawList()) {
			if(!ww.disposed)
				hierarchyAdd(list, ww);
		}
	}
	
	private Widget setupButton(int count, final String name, final String desc) {
		final double size = 10;
		Widget w = new Widget();
		final Tint tint = new Tint();
		w.addComponent(tint);
		w.addComponent(new DrawTexture().setTex(GuiEdit.tex(name)));
		w.transform.setSize(size, size).setPos(2 + count * 12, 11);
		w.regEventHandler(FrameEvent.class, (we, event) -> {
			Widget target = getAccessTarget();
			tint.enabled = target != null && target.visible;
			
			if(event.hovering) {
				Font.font.draw(desc, size / 2, size, 10, 0xffffff, Align.CENTER);
			}
		});
		
		addWidget(w);
		return w;
	}
	
	private class SingleWidget extends Widget {
		int hierLevel;
		Widget target;
		
		boolean modified = false;
		boolean on = true;
		
		final ResourceLocation 
			vis_on = GuiEdit.tex("vis_on"), 
			vis_off = GuiEdit.tex("vis_off");
		
		TextBox box;
		
		public SingleWidget(Widget w) {
			transform.width = 80;   
			transform.height = 12;
			
			hierLevel = w.getHierarchyLevel();
			target = w;
			
			regEventHandler(FrameEvent.class, (ww, event) -> {
				double r = 1, g = 1, b = 1;
				double brightness = event.hovering ? .5 : .3;
				if(target.isFocused()) {
					brightness *= 1.6;
					r = b = .6;
				}
				if(modified) {
					r = g = 1;
					b = 0;
				}
				GL11.glColor4d(r, g, b, brightness);
				HudUtils.colorRect(0, 0, ww.transform.width, ww.transform.height);
			});
			
			regEventHandler(MouseDownEvent.class, (ww, ee) -> { guiEdit.toEdit.gainFocus(target); });
			
			{
				Widget eye = new Widget();
				eye.transform.setSize(10, 10).setPos(1, 1);
				eye.addComponent(new Tint());
				eye.addComponent(new DrawTexture().setTex(vis_on));
				eye.regEventHandler(MouseDownEvent.class, (ww, e) -> {
					on = !on;
					target.visible = on;
					DrawTexture.get(ww).setTex(on ? vis_on : vis_off);
				});
				addWidget(eye);
			}
			
			addWidget(new Name());
		}
		
		private class Name extends Widget {
			
			int slowdown;
			
			public Name() {
				box = new TextBox().setSize(10);
				box.content = target.getName();
				transform.x = 14 + hierLevel * 6;
				transform.setSize(70, 10);
			}
			
			@Override
			public void onAdded() {
				regEventHandler(FrameEvent.class, (w, e) -> {
					if(++slowdown == 100) {
						slowdown = 0;
						transform.x = 14 + hierLevel * 6;
						w.dirty = true;
					}
				});
				regEventHandler(ConfirmInputEvent.class, (w, e) -> {
					if(target.rename(box.content)) {
						modified = false;
					}
				});
				regEventHandler(ChangeContentEvent.class, (w, e) -> {
					modified = true;
				});
				regEventHandler(GainFocusEvent.class, (w, e) -> {
					SingleWidget.this.postEvent(new MouseDownEvent(0, 0));
				});
				addComponent(box);
			}
		}
	}

}
