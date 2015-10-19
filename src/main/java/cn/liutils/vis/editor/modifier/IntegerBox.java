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
package cn.liutils.vis.editor.modifier;

import java.lang.reflect.Field;

import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.Transform.HeightAlign;
import cn.liutils.cgui.gui.component.Transform.WidthAlign;
import cn.liutils.vis.editor.common.VEVars;

/**
 * @author WeAthFolD
 */
public class IntegerBox extends BoxBase {
	
	public IntegerBox(Field _field, Object _instance) {
		transform.setSize(45, 14);
		
		IntegerModifier modifier = new IntegerModifier(_field, _instance);
		modifier.transform.alignWidth = WidthAlign.CENTER;
		modifier.transform.alignHeight = HeightAlign.CENTER;
		addWidget(modifier);
	}
	
}
