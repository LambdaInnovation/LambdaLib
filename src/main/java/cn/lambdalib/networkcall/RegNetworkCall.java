/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.networkcall;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.lambdalib.networkcall.s11n.StorageOption;
import cpw.mods.fml.relauncher.Side;

/**
 * @deprecated NetworkCall is deprecated due to performance and flexibility issues.
 * Consider using {@link cn.lambdalib.s11n.network.NetworkMessage} instead!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Deprecated
public @interface RegNetworkCall {
    // WARNING: Don't modify this class!
    // This annotation is used with ASM library.
    // The class name and method name are hard-coded.

    // For static method, use the default NULL.
    // For non-static method, you must use another option.
    StorageOption.Option thisStorage() default StorageOption.Option.NULL;

    // The side is the callee's side.
    // If a method is call by client and run in server, the side should be
    // SERVER.
    Side side();
}
