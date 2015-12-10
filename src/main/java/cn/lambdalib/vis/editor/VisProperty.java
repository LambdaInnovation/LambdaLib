package cn.lambdalib.vis.editor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Supply extra information for {@link cn.lambdalib.vis.editor.ObjectEditor}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VisProperty {

	/**
	 * @return Whether this field is excluded.
	 */
	boolean exclude() default false;

	/**
	 * @return The name of this property displayed in the editor tab. If not supplied,
	 * 	will use the field name.
	 */
	String name() default "";

}
