package cn.liutils.vis.editor.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import cn.liutils.core.LIUtils;
import cn.liutils.vis.editor.common.widget.WindowHierarchy.Element;
import cn.liutils.vis.editor.property.CompTransformProperty;
import cn.liutils.vis.editor.property.IntegerProperty;
import cn.liutils.vis.editor.property.RealProperty;
import cn.liutils.vis.editor.property.Vec3Property;
import cn.liutils.vis.model.CompTransform;
import net.minecraft.util.Vec3;

public class EditorHelper {
	
	private static Map<Class, IElementFactory> handled = new HashMap();
	
	public interface IElementFactory {
		Element create(VisEditable anno, Field f, Object instance);
	}

	public static void addHandledType(Class<?> type, IElementFactory factory) {
		handled.put(type, factory);
	}
	
	private static Element create(VisEditable anno, Field f, Object instance) {
		Class type = f.getType();
		while(type != null) {
			if(handled.containsKey(type))
				return handled.get(type).create(anno, f, instance);
			type = type.getSuperclass();
		}
		return null;
	}
	
	private static String ensureNamed(VisEditable anno, Field f) {
		return anno.value().equals("") ? f.getName() : anno.value();
	}
	
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface VisEditable {
		String value() default "";
	}

	public static void initHierarchy(IHierarchy hier, Object editable) {
		Class type = editable.getClass();
		for(Field f : type.getFields()) {
			if(f.isAnnotationPresent(VisEditable.class)) {
				VisEditable anno = f.getAnnotation(VisEditable.class);
				Element elem = create(anno, f, editable);
				if(elem != null) {
					hier.addElement(elem);
				} else {
					LIUtils.log.error("Unsupported type " + type + " in VisEditor");
				}
			}
		}
	}
	
	static {
		IElementFactory fac;
		{
			fac = (anno, f, instance) -> {
				return new RealProperty(ensureNamed(anno, f), f, instance);
			};
			addHandledType(Float.class, fac);
			addHandledType(float.class, fac);
			addHandledType(Double.class, fac);
			addHandledType(double.class, fac);
		}
		{
			fac = (anno, f, instance) -> {
				return new IntegerProperty(ensureNamed(anno, f), f, instance);
			};
			addHandledType(Integer.class, fac);
			addHandledType(int.class, fac);
			// TODO support short and byte
		}
		{
			fac = (anno, f, instance) -> {
				return new Vec3Property(ensureNamed(anno, f), f, instance);
			};
			addHandledType(Vec3.class, fac);
		}
		{
			fac = (anno, f, instance) -> {
				return new CompTransformProperty(ensureNamed(anno, f), f, instance);
			};
			addHandledType(CompTransform.class, fac);
		}
	}
	
}
