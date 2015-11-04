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

import cn.lambdalib.vis.editor.common.VEVars;
import cn.lambdalib.vis.editor.common.widget.WindowHierarchy.Folder;
import cn.lambdalib.vis.model.CompTransform;
import cn.liutils.util.generic.RegistryUtils;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 */
public class CompTransformProperty extends Folder {
	
	static final ResourceLocation ICON = VEVars.tex("hierarchy/comp_transform");

	static Field _transform, _pivotPt, _rotation, _scale;
	static {
		try {
			Class c = CompTransform.class;
			_transform = c.getField("transform");
			_pivotPt = c.getField("pivotPt");
			_rotation = c.getField("rotation");
			_scale = c.getField("scale");
			
			_transform.setAccessible(true);
			_pivotPt.setAccessible(true);
			_rotation.setAccessible(true);
			_scale.setAccessible(true);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	final Field field;
	final Object instance;
	
	public CompTransformProperty(String _name, Field _field, Object _instance) {
		super(_name, ICON);
		field = _field;
		instance = _instance;
		try {
			CompTransform ct = (CompTransform) field.get(instance);
			addElement(new Vec3Property("Transform", _transform, ct));
			addElement(new Vec3Property("Pivot", _pivotPt, ct));
			addElement(new Vec3Property("Rotation", _rotation, ct));
			addElement(new RealProperty("Scale", _scale, ct));
		} catch(Exception e) {
			throw new RuntimeException("Creating CompTransformProperty", e);
		}
	}

}
