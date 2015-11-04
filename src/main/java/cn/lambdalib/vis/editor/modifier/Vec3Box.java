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

import cn.liutils.util.generic.RegistryUtils;
import net.minecraft.util.Vec3;

/**
 * @author WeAthFolD
 */
public class Vec3Box extends BoxBase {
	
	static Field _fx, _fy, _fz;
	static {
		// TODO make sure
		_fx = RegistryUtils.getObfField(Vec3.class, "xCoord", "idk");
		_fy = RegistryUtils.getObfField(Vec3.class, "yCoord", "idk");
		_fz = RegistryUtils.getObfField(Vec3.class, "zCoord", "idk");
	}
	
	private final Field field;
	private final Object instance;
	private final Vec3 target;
	
	public Vec3Box(Field _field, Object _instance) {
		field = _field;
		instance = _instance;
		transform.setSize(50, 45);
		
		try {
			target = (Vec3) field.get(instance);
		} catch (Exception e) {
			throw new RuntimeException("Can't get vec3", e);
		}
		
		text("X", 2, 3);
		modifier(_fx, 10, 2);
		
		text("Y", 2, 18);
		modifier(_fy, 10, 17);
		
		text("Z", 2, 33);
		modifier(_fz, 10, 32);
	}
	
	private void modifier(Field _field, double x, double y) {
		RealModifier mod = new RealModifier(_field, target);
		mod.transform.setPos(x, y);
		addWidget(mod);
	}

}
