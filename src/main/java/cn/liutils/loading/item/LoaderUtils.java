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

import net.minecraft.util.Vec3;
import cn.liutils.loading.Loader.ObjectNamespace;

/**
 * @author WeAthFolD
 *
 */
public class LoaderUtils {

	public static Vec3 loadVec3(ObjectNamespace ns, String ...searchRule) {
		Object[] objs = new Object[searchRule.length + 1];
		for(int i = 0; i < searchRule.length; ++i) {
			objs[i] = searchRule[i];
		}
		Double x, y, z;
		
		int len = searchRule.length;
		objs[len] = 0;
		x = ns.getDouble(objs);
		
		objs[len] = 1;
		y = ns.getDouble(objs);
		
		objs[len] = 2;
		z = ns.getDouble(objs);
		
		if(x != null && y != null && z != null) {
			return Vec3.createVectorHelper(x, y, z);
		}
		return null;
	}
	
}
