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
package cn.lambdalib.networkcall;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.lambdalib.networkcall.s11n.StorageOption;
import cpw.mods.fml.relauncher.Side;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RegNetworkCall {
    //WARNING: Don't modify this class! 
    //This annotation is used with ASM library. 
    //The class name and method name are hard-coded.
    
    //For static method, use the default NULL.
    //For non-static method, you must use another option.
    StorageOption.Option thisStorage() default StorageOption.Option.NULL;
    
    //The side is the callee's side.
    //If a method is call by client and run in server, the side should be SERVER.
    Side side();
}
