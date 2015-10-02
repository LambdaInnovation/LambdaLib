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
package cn.liutils.core.entity;

/**
 * A simple sample point with a timer, data used by EntityTrail.
 * @author WeAthFolD
 */
public class SamplePoint {

	public double x, y, z;
	public int tick;

	public SamplePoint(double p1, double p2, double p3, int t) {
		x = p1;
		y = p2;
		z = p3;
		tick = t;
	}

}
