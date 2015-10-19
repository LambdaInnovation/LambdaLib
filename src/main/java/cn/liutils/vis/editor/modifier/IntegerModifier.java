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

import cn.liutils.cgui.gui.Widget;
import cn.liutils.cgui.gui.component.DrawTexture;
import cn.liutils.cgui.gui.component.TextBox;
import cn.liutils.cgui.gui.event.ChangeContentEvent;
import cn.liutils.cgui.gui.event.ChangeContentEvent.ChangeContentHandler;
import cn.liutils.cgui.gui.event.ConfirmInputEvent;
import cn.liutils.cgui.gui.event.ConfirmInputEvent.ConfirmInputHandler;
import cn.liutils.core.LIUtils;
import cn.liutils.vis.editor.common.VEVars;

/**
 * @author WeAthFolD
 */
public class IntegerModifier extends ModifierBase {
	
	public IntegerModifier(Field _field, Object _instance) {
		super(_field, _instance);
	}

	@Override
	protected void setValue(String content) throws Exception {
		field.set(instance, Integer.valueOf(content));
	}
	
}
