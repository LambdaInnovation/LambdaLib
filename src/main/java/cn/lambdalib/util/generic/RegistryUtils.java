package cn.lambdalib.util.generic;

import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.lambdalib.util.deprecated.TypeHelper;
import net.minecraft.util.ResourceLocation;

public class RegistryUtils {
    
    public static InputStream getResourceStream(ResourceLocation res) {
        try {
            String domain = res.getResourceDomain(), path = res.getResourcePath();
            return RegistryUtils.class.getResourceAsStream("/assets/" + domain + "/" + path);
        } catch(Exception e) {
            throw new RuntimeException("Invalid resource " + res, e);
        }
    }
    
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CopyIgnore {}
    
    public static <T> T copy(T object) throws Exception {
        T another = (T) object.getClass().newInstance();
        for(Field f : another.getClass().getFields()) {
            if(!f.isAnnotationPresent(CopyIgnore.class) && TypeHelper.isTypeSupported(f.getType())) {
                f.set(another, f.get(object));
            }
        }
        return another;
    }

    public static Field getObfField(Class cl, String normName, String obfName) {
        Field f = null;
        try {
            try {
                f = cl.getDeclaredField(normName);
            } catch(Exception e) {}
            
            if(f == null)
                f = cl.getDeclaredField(obfName);
            f.setAccessible(true);
            return f;
        } catch(Exception e) {
            return null;
        }
    }
    
    public static Method getMethod(Class cl, String methodName, String obfName, Class ...parameterTypes) {
        Method m = null;
        try {
            try {
                m = cl.getDeclaredMethod(methodName, parameterTypes);
            } catch(Exception e) {}
            
            if(m == null)
                m = cl.getDeclaredMethod(obfName, parameterTypes);
            
            m.setAccessible(true);
            return m;
        } catch(Exception e) {
            return null;
        }
    }
    
    public static <T> T getFieldInstance(Class cl, Object instance, String name) {
        return getFieldInstance(cl, instance, name, name);
    }
    
    public static <T> T getFieldInstance(Class cl, Object instance, String normName, String obfName) {
        Field f = getObfField(cl, normName, obfName);
        try {
            return (T) f.get(instance);
        } catch(Exception e) {
            return null;
        }
    }
    
}
