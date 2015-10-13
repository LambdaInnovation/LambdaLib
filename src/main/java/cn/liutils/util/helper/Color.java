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
package cn.liutils.util.helper;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author WeAthFolD
 */
public class Color {
	
	public double r, g, b, a = 1.0;
	
	public Color(double _r, double _g, double _b, double _a) {
		setColor4d(_r, _g, _b, _a);
	}
	
	public Color() {
		this(1, 1, 1, 1);
	}
	
	public Color(int hex) {
		fromHexColor(hex);
	}
	
	public void from(Color c) {
		this.r = c.r;
		this.g = c.g;
		this.b = c.b;
		this.a = c.a;
	}
	
	public Color setColor4i(int r, int g, int b, int a) {
		setColor4d(r / 255.0, g / 255.0, b / 255.0, a / 255.0);
		return this;
	}
	
	public Color setColor4d(double _r, double _g, double _b, double _a) {
		r = _r;
		g = _g;
		b = _b;
		a = _a;
		return this;
	}
	
	public Color fromHexColor(int hex) {
		setColor4i((hex >> 16) & 0xFF, (hex >> 8) & 0xFF, hex & 0xFF, (hex >> 24) & 0xFF);
		return this;
	}
	
	public int asHexColor() {
		int ir = (int) (r * 255), ig = (int) (g * 255), ib = (int) (b * 255), ia = (int) (a * 255);;
		return (ir << 16) | (ig << 8) | (ib) | (ia << 24);
	}
	
	public int asHexWithoutAlpha() {
		int ir = (int)(r * 255) & 0xFF, ig = (int)(g * 255) & 0xFF, ib = (int)(b * 255) & 0xFF;
		return (ir << 16) | (ig << 8) | ib;
	}
	
	@SideOnly(Side.CLIENT)
	public void bind() {
		//System.out.println("BIND " + r + " " + g + " " + b + " " + a);
		GL11.glColor4d(r, g, b, a);
	}
	
	public Color copy() {
		return new Color(r, g, b, a);
	}
	
	public Color monoize() {
		double x = (r + g + b) / 3;
		return new Color(x, x, x, a);
	}
	
	public String toString() {
		return String.valueOf(asHexColor());
	}
	
	public static Color WHITE() {
		return new Color(1, 1, 1, 1);
	}
	
}
