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
package cn.liutils.vis.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WeAthFolD
 */
public class PartedModel implements IModel {
	
	Map<String, IModel> childs = new HashMap();
	
	public PartedModel() {}
	
	public void addChild(String name, IModel child) {
		childs.put(name, child);
	}
	
	public <T extends IModel> T getChild(String name) {
		return (T) childs.get(name);
	}
	
	public void draw() {
		for(IModel model : childs.values())
			model.draw();
	}
	
}
