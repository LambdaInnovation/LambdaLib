/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class RegistryHelper {
    private final RegistryType parent;
    
    public RegistryHelper(RegistryType parent) {
        this.parent = parent;
    }

    /*
     * ID map
     */
    private Map<RegModInformation, Integer> idMap = new HashMap();
    private int firstID = 0;
    
    public int getNextIDForMod() {
        RegModInformation mod = parent.getCurrentMod();
        
        if (idMap.containsKey(mod)) {
            int ret = idMap.get(mod);
            idMap.put(mod, ret + 1);
            return ret;
        } else {
            int ret = firstID;
            idMap.put(mod, ret + 1);
            return ret;
        }
    }
    
    public void setFirstID(int id) {
        if (!idMap.isEmpty()) {
            throw new RuntimeException("Try to set initial id after registration.");
        }
        firstID = id;
    }
    
    /*
     * Mod field
     * Used in RegistryTypes that needs extra information (a field with a certain annotation) from the Mod instance.
     */
    private Class<? extends Annotation> modFieldAnno;
    private Map<RegModInformation, Object> modFieldCache = new HashMap();
    
    public void setModFieldAnnotation(Class<? extends Annotation> anno) {
        modFieldAnno = anno;
        modFieldCache.clear();
    }
    
    public Object getModField() {
        RegModInformation modInfo = parent.getCurrentMod();
        
        if (modFieldCache.containsKey(modInfo)) {
            return modFieldCache.get(modInfo);
        }
        Object mod = modInfo.getModInstance();
        Object ret = getFieldFromObject(mod, modFieldAnno);
        modFieldCache.put(modInfo, ret);
        return ret;
    }
    
    /**
     * Helper function used to get the value of a field (with anno) in the given class.
     * @param clazz
     * @param anno
     * @return
     */
    public final Object getFieldFromClass(Class<?> clazz, Class<? extends Annotation> anno) {
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(anno)) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    throw new RuntimeException("The field must be static.");
                }
                try {
                    field.setAccessible(true);
                    Object ret = field.get(null);//TODO should not use null
                    if (ret == null) {
                        ret = field.getType().newInstance();
                        field.set(null, ret);
                    }
                    return ret;
                } catch (Exception e) {
                    throw new RuntimeException("Can not get field from mod class: get failed.", e);
                }
            }
        }
        throw new RuntimeException("No field with required annotation " + anno + " exists in " + clazz);
    }
    
    public final Object getFieldFromObject(Object obj, Class<? extends Annotation> anno) {
        for (Field field : obj.getClass().getFields()) {
            if (field.isAnnotationPresent(anno)) {
                try {
                    field.setAccessible(true);
                    if (Modifier.isStatic(field.getModifiers())) {
                        Object ret = field.get(null);
                        if (ret == null) {
                            ret = field.getType().newInstance();
                            field.set(null, ret);
                        }
                        return ret;
                    } else {
                        Object ret = field.get(obj);
                        if (ret == null) {
                            ret = field.getType().newInstance();
                            field.set(obj, ret);
                        }
                        return ret;
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Can not get field from mod class: get failed.", e);
                }
            }
        }
        throw new RuntimeException("Can not get field from mod class: field not found.");
    }
}
