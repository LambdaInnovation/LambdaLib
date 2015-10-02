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
package cn.liutils.api.render.model;

import java.util.Collection;
import java.util.HashMap;

import javax.vecmath.Vector3d;

public abstract class Model {
	
	HashMap<String, Vector3d> parts;

	public Model() {}
	
	/**
	 * Data format:</br>
	 * <code>
	 * {</br>
	 * 	{ String , { double, double, double } },</br>
	 * 	{ ... }</br>
	 * }
	 * </code>
	 */
	public Model(Object[][] data) {
		parseData(data);
	}
	
	private void parseData(Object[][] data) {
		for(Object[] one : data) {
			String name = (String) one[0];
			double[] arr = (double[]) one[1];
			parts.put(name, new Vector3d(arr));
		}
	}
	
	public final Collection<String> getParts() {
		return parts.keySet();
	}
	
	public abstract void draw(String part);
	
	public abstract void drawAll();
	
	public final Vector3d getPivotPt(String part) {
		return parts.get(part);
	}

}
