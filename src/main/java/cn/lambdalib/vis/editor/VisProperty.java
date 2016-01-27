/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.editor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Supply extra information for {@link cn.lambdalib.vis.editor.ObjectEditor}. You can also use
 *  this annotation to force inspection of private values.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VisProperty {

    /**
     * @return The name of this property displayed in the editor tab. If not supplied,
     *     will use the field name.
     */
    String name() default "";

}
