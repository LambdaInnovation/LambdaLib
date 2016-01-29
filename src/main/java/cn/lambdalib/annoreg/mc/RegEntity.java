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
     *         #RegEntity.Render
     *         #SideOnly(Side.CLIENT)
     *         public static MyRender renderer;
     * }
     * </code>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Render {}
    
}
