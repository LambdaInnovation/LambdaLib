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
 * Registers the class as a listener into either FML or Forge bus.
 * FML event bus have been merged into Forge bus, but FML buss still leave for backward compatible.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegEventHandler {
    
    public enum Bus {
        FML,
        Forge,
    }
    
    Bus[] value() default {Bus.FML, Bus.Forge};
}
