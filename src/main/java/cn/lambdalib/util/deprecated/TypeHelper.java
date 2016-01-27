/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import cn.lambdalib.util.helper.Color;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 *
 */
@Deprecated
public class TypeHelper {
    
    private static Map<Class<?>, HandledType> staticHandlers = new HashMap();
    
    public static boolean isTypeSupported(Class<?> type) {
        return type.isEnum() || staticHandlers.containsKey(type);
    }
    
    public static boolean edit(Field f, Object instance, String value) {
        try {
            getHandledType(f).edit(f, instance, value);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public static boolean set(Field f, Object instance, Object value) {
        try {
            getHandledType(f).set(f, instance, value);
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * Get the value of the object. If possible return the object itself.
     */
    public static Object get(Field f, Object instance) {
        try {
            return getHandledType(f).get(f, instance);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get the copy of the object.
     */
    public static Object copy(Field f, Object instance) {
        try {
            return getHandledType(f).copy(f, instance);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String repr(Field f, Object instance) {
        try {
            return getHandledType(f).repr(f, instance);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return "<error>";
    }
    
    public static void addHandledType(HandledType ht, Class... classes) {
        for(Class c : classes) {
            if(staticHandlers.containsKey(c)) {
                throw new IllegalStateException("More than one HandledType for type " + c);
            }
            staticHandlers.put(c, ht);
        }
    }
    
    private static HandledType getHandledType(Field f) {
        Class cz = f.getType();
        HandledType ret = staticHandlers.get(cz);
        if(ret != null) return ret;
        
        //(Currently) Hard coded lazy creation.
        if(cz.isEnum()) {
            staticHandlers.put(cz, ret = new EnumTypeHandler(cz));
        }
        return ret;
    }
    
    static { //Dirty?Dirty...
        addHandledType(new HandledType() {

            @Override
            public void edit(Field f, Object instance, String value)
                    throws Exception {
                Double val = Double.valueOf(value);
                set(f, instance, val);
            }

            @Override
            public Object copy(Field f, Object instance) throws Exception {
                return new Double((Double) f.get(instance));
            }
            
        }, Double.class, Double.TYPE);
        
        addHandledType(new HandledType() {

            @Override
            public void edit(Field f, Object instance, String value)
                    throws Exception {
                Integer val = Integer.valueOf(value);
                set(f, instance, val);
            }

            @Override
            public Object copy(Field f, Object instance) throws Exception {
                return new Integer((Integer) f.get(instance));
            }
            
        }, Integer.class, Integer.TYPE);
        
        addHandledType(new HandledType() {

            @Override
            public void edit(Field f, Object instance, String value)
                    throws Exception {
                Float val = Float.valueOf(value);
                set(f, instance, val);
            }

            @Override
            public Object copy(Field f, Object instance) throws Exception {
                return new Float((Float) f.get(instance));
            }
            
        }, Float.class, Float.TYPE);
        
        addHandledType(new HandledType() {

            @Override
            public void edit(Field f, Object instance, String value)
                    throws Exception {
                set(f, instance, value);
            }

            @Override
            public Object copy(Field f, Object instance) throws Exception {
                return new String((String) f.get(instance));
            }
            
        }, String.class);
        
        addHandledType(new HandledType() {

            @Override
            public void edit(Field f, Object instance, String value)
                    throws Exception {
                set(f, instance, Boolean.valueOf(value));
            }

            @Override
            public Object copy(Field f, Object instance) throws Exception {
                return new Boolean((Boolean) f.get(instance));
            }
        }, Boolean.class, Boolean.TYPE);
        
        addHandledType(new HandledType() {

            @Override
            public void edit(Field f, Object instance, String value)
                    throws Exception {
                set(f, instance, new ResourceLocation(value));
            }

            @Override
            public Object copy(Field f, Object instance) throws Exception {
                ResourceLocation rl = (ResourceLocation) f.get(instance);
                return new ResourceLocation(rl.getResourceDomain(), rl.getResourcePath());
            }
        }, ResourceLocation.class);
        
        addHandledType(new HandledType() {

            @Override
            public void edit(Field f, Object instance, String value)
                    throws Exception {
                int hex = Integer.valueOf(value);
                f.set(instance, new Color(hex));
            }

            @Override
            public String repr(Field f, Object instance) throws Exception {
                return String.valueOf(((Color) f.get(instance)).asHexColor());
            }
            
            @Override
            public Object copy(Field f, Object instance) throws Exception {
                return ((Color)f.get(instance)).copy();
            }
            
        }, Color.class);
    }
    
}
