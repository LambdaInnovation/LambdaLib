package cn.lambdalib.ripple;


/**
 * Super class for all compiled function, used by code generator.
 * You should not directly implement this interface. Use NativeFunction instead.
 * @author acaly
 *
 */
public interface IFunction {
    
    Object call(Object[] args);
    
}
