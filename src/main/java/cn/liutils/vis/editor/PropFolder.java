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
package cn.liutils.vis.editor;

import java.util.ArrayList;
import java.util.List;

import cn.liutils.cgui.gui.Widget;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 */
public final class PropFolder implements IEditorProp {
	
	private List<IEditorProp> childs;

	@Override
	public Widget createPropEditor(Object object) {
		return null;
	}

	public void addChild(IEditorProp object) {
		if(childs == null)
			childs = new ArrayList();
		childs.add(object);
	}
	
	@Override
	public List<IEditorProp> getChilds() {
		return childs;
	}

	@Override
	public ResourceLocation getIcon() {
		return null;
	}

}
