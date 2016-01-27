/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

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
    Side[] side();

    /**
     * Populate this on a public static SimpleNetworkWrapper instance in your mod class,
     * and AR will record it and treat it as an internal registration source. You can 
     * later send messages registered using that SimpleNetworkWrapper.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface WrapperInstance {}
}
