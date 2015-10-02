package cn.liutils.ripple.impl.compiler;

import cn.liutils.ripple.IFunction;

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
