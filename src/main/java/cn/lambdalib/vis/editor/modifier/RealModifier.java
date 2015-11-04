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
package cn.lambdalib.vis.editor.modifier;

import java.lang.reflect.Field;

/**
 * @author WeAthFolD
 */
public class RealModifier extends ModifierBase {
	
	boolean isDouble;

	public RealModifier(Field _field, Object _instance) {
		super(_field, _instance);
		Class type = field.getType();
		isDouble = type == Double.class || type == Double.TYPE;
	}

	@Override
	protected void setValue(String content) throws Exception {
		if(isDouble)
			field.set(instance, Double.valueOf(content));
		else
			field.set(instance, Float.valueOf(content));
	}

}
