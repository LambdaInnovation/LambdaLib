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
package cn.lambdalib.vis.editor.property;

import java.lang.reflect.Field;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.vis.editor.common.VEVars;
import cn.lambdalib.vis.editor.common.widget.WindowHierarchy.Element;
import cn.lambdalib.vis.editor.modifier.IntegerBox;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 */
public class IntegerProperty extends Element {
	
	static final ResourceLocation ICON = VEVars.tex("hierarchy/integer");
	
	private final Field field;
	private final Object instance;
	
	public IntegerProperty(String _name, Field _field, Object _instance) {
		super(_name, ICON);
		field = _field;
		instance = _instance;
	}

	@Override
	public void onClick() {
		addAdditional(new IntegerBox(field, instance));
	}

}
