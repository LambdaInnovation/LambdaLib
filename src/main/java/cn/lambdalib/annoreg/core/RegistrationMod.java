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
package cn.lambdalib.annoreg.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate this on mod's main class to enable the AnnoReg and provide necessary information.
 * @author acaly
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegistrationMod {
    /**
     * Package prefix of the classes in this mod.
     * @return
     */
	String pkg();
	/**
	 * Resource folder name of this mod.  
	 * @return
	 */
	String res();
	/**
	 * An optional prefix of name in some registry types in case of name collision.
	 * See {@link RegWithName}.
	 * @return
	 */
	String prefix() default "";
}
