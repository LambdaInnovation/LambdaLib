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

import net.minecraft.item.ItemBlock;

/**
 * Registers a block. Put this on your static block instance.
 * e.g. @RegBlock public static MyBlock block; will construct a MyBlock() instance and reg it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegBlock {

    /**
     * Register block's oreDictionary.
     * @par oreDict name
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface OreDict {
        String value();
    }
    
    /**
     * Register the block's unlocalized name and icon name at once.
     * e.g. @RegBlock.BTName("fff") in mod "academy" will give unlocalized name "fff" and icon name "academy:fff".
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface BTName {
        String value();
    }
    
    /**
     * The ItemBlock class that this block will use.
     */
    Class<? extends ItemBlock> item() default ItemBlock.class;
    
}
