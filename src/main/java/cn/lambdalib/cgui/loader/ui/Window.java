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

import org.lwjgl.opengl.GL11;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.component.DrawTexture;
import cn.lambdalib.cgui.gui.component.Transform;
import cn.lambdalib.cgui.gui.event.DragEvent;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.cgui.gui.event.MouseDownEvent;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.helper.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

/**
 * @author WeAthFolD
 */
public class Window extends Widget {
	
	final boolean canClose;
	
	protected String name;
	
	final GuiEdit guiEdit;
	
	public Window(GuiEdit _guiEdit, final String _name, boolean _canClose, double[] defaultPos) {
		name = _name;
		canClose = _canClose;
		guiEdit = _guiEdit;
		//Vector2d pos = guiEdit.getDefaultPosition(name, defaultPos);
		transform.setPos(defaultPos[0], defaultPos[1]);
	}
	
	public Window(GuiEdit _guiEdit, final String _name, boolean _canClose) {
		this(_guiEdit, _name, _canClose, new double[] { 0, 0 });
	}
	
	@Override
	public void onAdded() {
		if(canClose) {
			 Widget close = new Widget();
			 close.transform.setSize(10, 10).setPos(transform.width - 12, 1);
			 close.addComponent(new DrawTexture().setTex(GuiEdit.tex("toolbar/close")));
			 close.listen(MouseDownEvent.class, (w, e) -> {
				 Window.this.dispose();
			 });
			 addWidget(close);
		}
		
		if(!this.isWidgetParent()) {
			this.listen(DragEvent.class, (w, e) -> {
				w.getGui().updateDragWidget();
				guiEdit.updateDefaultPosition(name, w.transform.x, w.transform.y);
			});
		}
		
		listen(FrameEvent.class, (w, e) -> {
			Transform t = w.transform;
			final double bar_ht = 10;
			
			GuiEdit.bindColor(2);
			HudUtils.colorRect(0, 0, t.width, bar_ht);
			
			GuiEdit.bindColor(1);
			HudUtils.colorRect(0, bar_ht, t.width, t.height - bar_ht);
			
			Font.font.draw(name, 10, 0, 10, 0x7fbeff);
		});
	}
	
	public GuiEdit gui() {
		return (GuiEdit) Minecraft.getMinecraft().currentScreen;
	}
	
	protected void drawSplitLine(double y) {
		Tessellator t = Tessellator.instance;
		GuiEdit.bindColor(3);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(1.5f);
		t.startDrawing(GL11.GL_LINES);
		t.addVertex(0, y, -90);
		t.addVertex(transform.width, y, -90);
		t.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
}
