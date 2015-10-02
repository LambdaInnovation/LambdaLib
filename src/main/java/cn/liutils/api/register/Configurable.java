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
package cn.liutils.api.register;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * (Inspired by HyperX)
 * Mark a variable so it can be automatically loaded by config.
 * Currently static fields only.
 * @see cn.liutils.api.LIGeneralRegistry#loadConfigurableClass
 * @author WeAthFolD
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configurable {

	/**
	 * Category of the settings shown in the configuration file.
	 */
	String category() default "general";

	/**
	 * The name of the settings.
	 */
	String key();

	/**
	 * Default value of the setting.
	 */
	String defValue() default "";

	/**
	 * Default value of the setting, as an integer.
	 */
	int defValueInt() default 0;
	
	/**
	 * Default value of the setting, as a boolean.
	 */
	boolean defValueBool() default false;

	/**
	 * Default value of the setting, as a double.
	 */
	double defValueDouble() default 0.0;
	
	/**
	 * Comment of the setting.
	 */
	String comment() default "";
}
