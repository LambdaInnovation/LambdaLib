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

import java.io.File;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.cgui.client.CGUILang;
import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.component.DrawTexture;
import cn.lambdalib.cgui.gui.component.TextBox;
import cn.lambdalib.cgui.gui.component.Tint;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.cgui.gui.event.MouseDownEvent;
import cn.lambdalib.cgui.loader.xml.CGUIDocWriter;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.helper.Color;
import cn.lambdalib.util.helper.Font;
import cn.lambdalib.util.helper.GameTimer;
import cn.lambdalib.util.helper.Font.Align;
import net.minecraft.client.Minecraft;

/**
 * @author WeAthFolD
 *
 */
public class SaveAsScreen extends Window {
	
	TextBox textBox;
	
	//String warnMessage;
	long lastWarningTime;

	public SaveAsScreen(GuiEdit _guiEdit) {
		super(_guiEdit, CGUILang.butSaveAs(), true);
		transform.setCenteredAlign();
		transform.setSize(100, 40);
		addWidgets();
	}
	
	private void addWidgets() {
		Widget tb = new Widget();
		tb.transform.setPos(10, 15).setSize(80, 10);
		
		DrawTexture drawer = new DrawTexture();
		drawer.setTex(null).setColor4d(1, 1, 1, 0.3);
		tb.addComponent(drawer);
		
		textBox = new TextBox();
		textBox.content = "cgui/unnamed_0.xml";
		textBox.size = 9;
		tb.addComponent(textBox);
		addWidget(tb);
		
		Widget button = new Widget();
		button.transform.setSize(18, 8).setPos(41, 30);
		
		Tint tint = new Tint();
		tint.idleColor = new Color(1, 1, 1, 0.3);
		button.addComponent(tint);
		
		button.listen(MouseDownEvent.class, (w, e) -> {
			File file = new File(textBox.content);
			if(file.exists()) {
				lastWarningTime = GameTimer.getAbsTime();
			} else {
				if(!CGUIDocWriter.save(guiEdit.toEdit, file))
					Minecraft.getMinecraft().thePlayer.sendChatMessage(CGUILang.commSaveFailed() + textBox.content);
				else
					Minecraft.getMinecraft().thePlayer.sendChatMessage(CGUILang.commSaved() + textBox.content);
				guiEdit.path = textBox.content;
				SaveAsScreen.this.dispose();
			}
		});
		
		button.listen(FrameEvent.class, (w, e) -> {
			Font.font.draw(CGUILang.butSave(), 9, 1, 6, 0xffffff, Align.CENTER);
			if(GameTimer.getAbsTime() - lastWarningTime < 1000L) {
				GL11.glColor4d(1, .3, .3, .3);
				HudUtils.colorRect(0, 0, w.transform.width, w.transform.height);
				GL11.glColor4d(1, 1, 1, 1);
			}
		});
		
		addWidget(button);
	}

}
