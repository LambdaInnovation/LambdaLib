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
package cn.liutils.vis.test;

import org.lwjgl.opengl.GL11;

import cn.annoreg.core.Registrant;
import cn.liutils.api.gui.AuxGui;
import cn.liutils.registry.AuxGuiRegistry.RegAuxGui;
import cn.liutils.util.client.HudUtils;
import cn.liutils.vis.curve.BezierCurve;
import net.minecraft.client.gui.ScaledResolution;

/**
 * @author WeAthFolD
 */
@Registrant
@RegAuxGui
public class BezierTest extends AuxGui {
	
	BezierCurve curve = new BezierCurve();
	
	public BezierTest() {
		curve.divideStep = 0.01;
		
		curve.addPoint(0, 0);
		curve.addPoint(1, 1);
		curve.addPoint(2, 0);
		curve.setCtrlPoint(0, 0, -1);
		curve.setCtrlPoint(1, 1.2, 1);
		curve.setCtrlPoint(2, 2, -1);
	}

	@Override
	public boolean isForeground() {
		return false;
	}

	@Override
	public void draw(ScaledResolution sr) {
		double s = 50;
		
		GL11.glPushMatrix();
		GL11.glTranslated(100, 100, 0);
		GL11.glScaled(s, -s, 1);
		GL11.glColor4d(1, 1, 1, 1);
		GL11.glPointSize(5f);
		GL11.glLineWidth(2f);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBegin(GL11.GL_LINE_STRIP);
		
		for(double x = -1; x <= 3; x += 0.03) {
			// System.out.println("drawing " + x + " " + curve.valueAt(x));
			GL11.glVertex2d(x, curve.valueAt(x));
		}
		GL11.glEnd();
		GL11.glColor4d(1, 0, 0, 1);
		GL11.glBegin(GL11.GL_POINTS);
		GL11.glVertex2d(0, 0);
		GL11.glVertex2d(1, 1);
		GL11.glVertex2d(2, 0);
		GL11.glEnd();
		
		GL11.glColor4d(1, 1, 0, 1);
		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex2d(0, 0);
		GL11.glVertex2d(0, -1);
		GL11.glVertex2d(1, 1);
		GL11.glVertex2d(1.2, 1);
		GL11.glVertex2d(2, 0);
		GL11.glVertex2d(2, -1);
		GL11.glEnd();
		
		GL11.glColor4d(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		
		GL11.glColor4d(1, 1, 1, .2);
		HudUtils.colorRect(0, 0, 1, 1);
		GL11.glPopMatrix();
	}

}
