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
package cn.liutils.vis.editor.property;

import java.lang.reflect.Field;

import cn.liutils.cgui.gui.Widget;
import cn.liutils.vis.editor.common.VEVars;
import cn.liutils.vis.editor.common.widget.WindowHierarchy.Element;
import cn.liutils.vis.editor.modifier.RealBox;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 */
public class RealProperty extends Element {
	
	static final ResourceLocation 
		ICON_FLOAT = VEVars.tex("hierarchy/float"),
		ICON_DOUBLE = VEVars.tex("hierarchy/double");
	
	private final Field field;
	private final Object instance;
	
	public RealProperty(String _name, Field _field, Object _instance) {
		super(_name, 
			_field.getType() == Double.class || 
			_field.getType() == Double.TYPE ? ICON_DOUBLE : ICON_FLOAT);
		field = _field;
		instance = _instance;
	}

	@Override
	public void onClick() {
		addAdditional(new RealBox(field, instance));
	}

}

