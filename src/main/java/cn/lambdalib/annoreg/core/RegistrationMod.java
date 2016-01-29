/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
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
