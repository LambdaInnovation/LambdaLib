/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import java.lang.reflect.Field;

import com.google.common.collect.HashBiMap;

/**
 * Generic handler for enum type.
 * @author WeAthFolD
 */
public class EnumTypeHandler extends HandledType {
    
    HashBiMap<Integer, String> map = HashBiMap.create();
    final Class enumClass;
    
    public EnumTypeHandler(Class cz) {
        int i = 0;
        enumClass = cz;
        for(Object e : cz.getEnumConstants()) {
            map.put(i++, e.toString());
        }
    }

    @Override
    public void edit(Field f, Object instance, String value) throws Exception {
        Integer i = map.inverse().get(value);
        if(i == null) {
            throw new RuntimeException();
        }
        f.set(instance, enumClass.getEnumConstants()[i]);
    }

    @Override
    public String repr(Field f, Object instance) throws Exception {
        return f.get(instance).toString();
    }

    @Override
    public Object copy(Field f, Object instance) throws Exception {
        return f.get(instance);
    }

    @Override
    public void set(Field f, Object instance, Object value) throws Exception {
        f.set(instance, value);
    }

}
