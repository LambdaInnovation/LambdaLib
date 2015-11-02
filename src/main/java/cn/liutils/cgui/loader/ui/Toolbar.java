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

import java.util.Map.Entry;

import org.lwjgl.opengl.GL11;

import cn.liutils.cgui.client.CGUILang;
import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.Transform;
import cn.liutils.cgui.gui.event.FrameEvent;
import cn.liutils.cgui.gui.event.LostFocusEvent;
import cn.liutils.cgui.gui.event.MouseDownEvent;
import cn.liutils.cgui.loader.CGUIEditor;
import cn.liutils.util.client.HudUtils;
import cn.liutils.util.helper.Font;
import cn.liutils.util.helper.Font.Align;

/**
 * @author WeAthFolD
 *
 */
public class Toolbar extends Window {
	
	boolean isLocked = false;
	
	public Toolbar(final GuiEdit guiEdit) {
		super(guiEdit, CGUILang.guiToolbar(), false, new double[] { 10, 10 });
		transform.setSize(200, 30);
		
		addWidget(new Button(0, "save", CGUILang.butSave()) {
			@Override public void triggerEvent() {
				if(guiEdit.path != null) {
					guiEdit.saveResult();
				} else {
					guiEdit.getGui().addWidget("SaveAs", new SaveAsScreen(guiEdit));
				}
			}
		});
		addWidget(new Button(1, "saveas", CGUILang.butSaveAs()) {
			@Override public void triggerEvent() {
				guiEdit.getGui().addWidget("SaveAs", new SaveAsScreen(guiEdit));
			}
		});
		addWidget(new Button(2, "add", CGUILang.butAdd()) {
			@Override public void triggerEvent() {
				isLocked = true;
				Widget tl = new TemplateList();
				Toolbar.this.addWidget(tl);
				getGui().gainFocus(tl);
			}
		});
		addWidget(new Button(3, "hierarchy", CGUILang.butHierarchy()) {
			@Override public void triggerEvent() {
				if(!getGui().hasWidget("hierarchy")) {
					getGui().addWidget("hierarchy", new Hierarchy(gui()));
				}
			}
		});
		addWidget(new Button(4, "background", CGUILang.butBackground()) {
			@Override public void triggerEvent() {
				guiEdit.toggleBlack = !guiEdit.toggleBlack;
			}
		});
	}
	
	class Button extends Widget {
		public Button(int i, final String tn, final String name) {
			transform.setSize(18, 18).setPos(5 + i * 20, 10);
			this.addComponent(new DrawTexture().setTex(GuiEdit.tex("toolbar/" + tn)).setColor4i(127, 190, 255, 255));
			regEventHandler(FrameEvent.class, (w, e) -> {
				if(e.hovering && !isLocked) {
					GL11.glColor4d(1, 1, 1, .5);
					HudUtils.colorRect(0, 0, transform.width, transform.height);
					Font.font.draw(name, 9, 19, 10, 0x9fceff, Align.CENTER);
				}
			});
			regEventHandler(MouseDownEvent.class, (w, e) -> {
				if(!isLocked)
					triggerEvent();
			});
		}
		
		public void triggerEvent() {}
	}
	
	class TemplateList extends Widget {
		public TemplateList() {
			transform.x = 30;
			transform.y = 30;
			
			final double width = 50, height = 12; //For a single subelement
			
			int i = 0;
			for(final Entry<String, Widget> e : CGUIEditor.getTemplates()) {
				Widget one = new Widget();
				final String name = e.getKey();
				one.regEventHandler(FrameEvent.class, (w, event) -> {
					GL11.glColor4d(.3, .3, .3, event.hovering ? 0.8 : 0.5);
					HudUtils.colorRect(0, 0, w.transform.width, w.transform.height);
					
					Font.font.draw(name, 25, 1.5, 10, 0x98b8e2, Align.CENTER);
				});
				one.regEventHandler(MouseDownEvent.class, (w, event) -> {
					isLocked = false;
					TemplateList.this.dispose();
					gui().toEdit.addWidget(CGUIEditor.createFromTemplate(name));
				});
				Transform ot = one.transform;
				ot.x = 0;
				ot.y = (i++) * height;
				ot.width = width;
				ot.height = height;
				addWidget(one);
			}
			
			regEventHandler(LostFocusEvent.class, (w, event) -> {
				isLocked = false;
				w.dispose();
			});
		}
	}
	
}
