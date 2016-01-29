/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author WeAthFolD
 */
@Deprecated
public abstract class HandledType {
    
    public void set(Field f, Object instance, Object value) throws Exception {
        f.set(instance, value);
    }
    
    public abstract void edit(Field f, Object instance, String value) throws Exception;
    
    public String repr(Field f, Object instance) throws Exception {
        return f.get(instance).toString();
    }
    
    public Object get(Field f, Object instance) throws Exception {
        return f.get(instance);
    }
    
    public abstract Object copy(Field f, Object instance) throws Exception;
    
}
