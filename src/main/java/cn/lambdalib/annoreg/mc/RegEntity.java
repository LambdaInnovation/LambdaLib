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
package cn.lambdalib.annoreg.mc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers an entity class. Populated on entity type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegEntity {
	
	int trackRange() default 32;
	int freq() default 3;
	boolean updateVel() default true;
	
	/**
	 * Whether we don't register the entity and just register the entity render or not.
	 */
	boolean clientOnly() default false;
	
	/**
	 * Mark that this entity needs to register a render. Populated on entity class.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface HasRender {}

	/**
	 * Used to mark the render instance inside the entity class. Instance must be public static.
	 * e.g.</br>
	 * <code>
	 * #RegistrationClass
	 * #RegEntity
	 * #RegEntity.HasRender
	 * public class MyEntity {
	 * 		#RegEntity.Render
	 * 		#SideOnly(Side.CLIENT)
	 * 		public static MyRender renderer;
	 * }
	 * </code>
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Render {}
	
}
