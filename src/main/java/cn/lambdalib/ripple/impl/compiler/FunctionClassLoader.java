/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.ripple.impl.compiler;

import cn.lambdalib.ripple.IFunction;

/**
 * The class loader used to dynamically load IFunction classes.
 * @author acaly
 *
 */
public final class FunctionClassLoader extends ClassLoader {
    
    public FunctionClassLoader() {
        super(IFunction.class.getClassLoader());
    }
    
    public Class<?> defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
    
}
