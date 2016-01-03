/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under  
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
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
