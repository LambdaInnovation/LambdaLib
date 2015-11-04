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
package cn.lambdalib.vis.editor.animation;

import java.lang.reflect.Field;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.component.DrawTexture;
import cn.lambdalib.cgui.gui.component.Transform.HeightAlign;
import cn.lambdalib.cgui.gui.component.Transform.WidthAlign;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.helper.Font;
import cn.lambdalib.util.helper.Font.Align;
import cn.lambdalib.vis.curve.IFittedCurve;
import cn.lambdalib.vis.curve.IFittedCurve.Point;
import cn.lambdalib.vis.editor.common.VEVars;
import cn.lambdalib.vis.editor.common.widget.Window;
import cn.lambdalib.vis.editor.modifier.RealModifier;

/**
 * @author WeAthFolD
 */
public class CurveView extends Window {
	
	static final double 
		DEF_L = 0, DEF_R = 10, DEF_U = 10, DEF_D = 0;
	
	private double l = DEF_L, r = DEF_R, u = DEF_U, d = DEF_D;

	public final IFittedCurve curve;
	public double precision = 0.01;
	
	public int xdiv = 10, ydiv = 10;
	
	public CurveView(IFittedCurve _curve) {
		super("Curve Viewer");
		curve = _curve;
		
		transform.setSize(300, 156);
		initTopButton(TopButtonType.MINIMIZE);
		
		initViewArea();
		initWidgets();
	}
	
	private void initViewArea() {
		Widget area = new Widget();
		
		area.transform.alignWidth = WidthAlign.CENTER;
		area.transform.setPos(6, 10).setSize(270, 110);
		
		area.listen(FrameEvent.class, this::onAreaDraw);
		
		body.addWidget(area);
	}
	
	/**
	 * Hard-coded area draw callback. Used to override drawing method.
	 */
	protected void onAreaDraw(Widget w, FrameEvent event) {
		// System.out.println("Drawing")
		GL11.glDepthMask(true);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glPushMatrix();
		// Map to widget size
		
		GL11.glTranslated(0, 0, -10);
		
		GL11.glScaled(w.transform.width, w.transform.height, 1);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		GL11.glDepthFunc(GL11.GL_ALWAYS);
		// Background
		GL11.glColor4d(.3, .3, .3, 1);
		HudUtils.colorRect(0, 0, 1, 1);
		
		// Draw curve
		GL11.glDepthFunc(GL11.GL_EQUAL);
		GL11.glLineWidth(2f);
		GL11.glColor4d(1, 1, 1, 1);
		GL11.glBegin(GL11.GL_LINE_STRIP);
		for(double x = 0; x <= 1.05; x += precision) {
			double rx = l + (r - l) * x;
			double ry = curve.valueAt(rx);
			double y = ry2y(ry);
			GL11.glVertex2d(x, y);
		}
		GL11.glEnd();
		
		// Draw points
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glPointSize(4f);
		GL11.glColor4d(1, 0, 0, 1);
		GL11.glBegin(GL11.GL_POINTS);
		int max = curve.pointCount();
		for(int i = 0; i < max ; ++i) {
			Point p = curve.getPoint(i);
			if(l <= p.x && p.x <= r && d <= p.y && p.y <= u) {
				GL11.glVertex2d(rx2x(p.x), ry2y(p.y));
			}
		}
		GL11.glEnd();
		
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		drawRuler(w);
	}
	
	private void drawRuler(Widget w) {
		// x
		for(double x = 0; x <= 1.0; x += 1.0 / xdiv) {
			Font.font.draw(String.format("%.2f", x2rx(x)), x * w.transform.width, w.transform.height, 9, 0xaaaaaa, Align.CENTER);
		}
		// y
		for(double y = 0; y <= 1.0; y += 1.0 / ydiv) {
			Font.font.draw(String.format("%.2f", y2ry(y)), 0, (1 - y) * w.transform.height - 5, 9, 0xaaaaaa, Align.RIGHT);
		}
		// refline
		//GL11.glDepthFunc(GL11.GL_ALWAYS);
		GL11.glPushMatrix();
		GL11.glTranslated(0, 0, 0);
		GL11.glColor4d(.1, .1, .1, .3);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_LINES);
		for(double x = 0; x <= 1.0; x += 1.0 / xdiv) {
			GL11.glVertex2d(x * w.transform.width, 0);
			GL11.glVertex2d(x * w.transform.width, w.transform.height);
		}
		for(double y = 0; y <= 1.0; y += 1.0 / ydiv) {
			GL11.glVertex2d(0, y * w.transform.height);
			GL11.glVertex2d(w.transform.width, y * w.transform.height);
		}
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
	}
	
	protected double ry2y(double ry) {
		return 1 - ((u - d) == 0 ? 0 : (ry - d) / (u - d));
	}
	
	protected double rx2x(double rx) {
		return (r - l) == 0 ? 0 : (rx - l) / (r - l);
	}
	
	protected double x2rx(double x) {
		return l + x * (r - l);
	}
	
	protected double y2ry(double y) {
		return d + y * (u - d);
	}
	
	private void initWidgets() {
		double x = 40, s = 65;
		doubleModifier(x, -4, "l", "minX");
		doubleModifier(x += s, -4, "r", "maxX");
		doubleModifier(x += s, -4, "d", "minY");
		doubleModifier(x += s, -4, "u", "maxY");
	}
	
	// Helper macros
	private void doubleModifier(double x, double y, String fieldName, String desc) {
		try {
			Field f = getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			RealModifier mod = new RealModifier(f, this);
			mod.transform.setPos(x, y);
			mod.transform.alignHeight = HeightAlign.BOTTOM;
			body.addWidget(mod);
			mod.listen(FrameEvent.class, (w, e) -> {
				Font.font.draw(desc, -3, 0, 10, 0xffffff, Align.RIGHT);
			});
		} catch(Exception e) {
			LambdaLib.log.error("Init CurveView", e);
		}
	}

}
