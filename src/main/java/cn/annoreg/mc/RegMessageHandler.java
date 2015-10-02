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
package cn.annoreg.mc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Register a IMessage, into its mod unique underlying SimpleNetworkWrapper. 
 * It should be guaranteed that client and server mod have the exactly same message types&counts,
 * so that AnnoReg can keep the discriminators ordered. Otherwise, using the annotation will cause
 * inconsistent discriminators in client and server.
 * @author WeathFolD, acaly
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegMessageHandler {
	public enum Side {
		SERVER,
		CLIENT,
	}
	
	Class<?> msg();
	Side side();

	/**
	 * Populate this on a public static SimpleNetworkWrapper instance in your mod class,
	 * and AR will record it and treat it as an internal registration source. You can 
	 * later send messages registered using that SimpleNetworkWrapper.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface WrapperInstance {}
}
