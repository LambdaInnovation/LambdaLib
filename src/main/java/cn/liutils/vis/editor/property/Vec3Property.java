package cn.liutils.vis.editor.property;

import java.lang.reflect.Field;

import cn.liutils.vis.editor.common.VEVars;
import cn.liutils.vis.editor.common.widget.WindowHierarchy.Element;
import cn.liutils.vis.editor.modifier.Vec3Box;
import net.minecraft.util.ResourceLocation;

public class Vec3Property  extends Element {
	
	static final ResourceLocation ICON = VEVars.tex("hierarchy/vec3");
	
	private final Field field;
	private final Object instance;
	
	public Vec3Property(String _name, Field _field, Object _instance) {
		super(_name, ICON);
		field = _field;
		instance = _instance;
	}

	@Override
	public void onClick() {
		addAdditional(new Vec3Box(field, instance));
	}

}
