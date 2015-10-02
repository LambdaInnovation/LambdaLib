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
package cn.liutils.loading.item;

import net.minecraft.item.Item;
import cn.liutils.loading.Loader.ObjectNamespace;

/**
 * @author WeAthFolD
 */
public abstract class ItemLoadRule<T extends Item> {
	
	public abstract void load(T item, ObjectNamespace ns, String name) throws Exception;
	
	public void finishedLoad(T item, ObjectNamespace ns, String name) throws Exception {}
	
	public boolean applyFor(Item item, ItemLoader loader, String name) {
		return true;
	}
	
	protected String getNamespace(ObjectNamespace ns) {
		String name = ns.getString("namespace");
		return name == null ? "minecraft" : name;
	}
	
}
