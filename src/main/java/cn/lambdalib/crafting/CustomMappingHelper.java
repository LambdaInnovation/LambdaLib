/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.crafting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import cn.lambdalib.core.LambdaLib;

public class CustomMappingHelper {

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RecipeName {
        String value();
    }

    public static void addMapping(RecipeRegistry reg, Class klass) {
        try {
            for (Field f : klass.getFields()) {
                if (f.isAnnotationPresent(RecipeName.class)) {
                    RecipeName anno = f.getAnnotation(RecipeName.class);
                    reg.map(anno.value(), f.get(null));
                }
            }
        } catch (Exception e) {
            LambdaLib.log.error("An error occured analyzing recipe custom mapping");
            e.printStackTrace();
        }
    }

}
