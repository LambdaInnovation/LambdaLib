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
 * Registers an item.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegItem {
    
    /**
     * Add this annotation to set unlocalized name and texture name.
     * tname is set to mod.res (+ ":") + value.
     * uname is set to mod.prefix + value.
     * If value is empty, the RegItem.name will be used.
     * @author acaly
     *
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface UTName {
        String value();
    }

    /**
     * Reg this item into oreDictionary.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface OreDict {
        String value();
    }
    
    /**
     * Populated on Item instance, indicating this item has an render.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface HasRender {}

    /**
     * Used with @HasRender on public static <? extends IItemRenderer> classes, will find that instance and register it.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Render {}
}
