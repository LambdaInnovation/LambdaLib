package cn.lambdalib.s11n;

import cn.lambdalib.s11n.SerializeStrategy.ExposeStrategy;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SerializationHelper {

    private Set<Class> serializeTypes = new HashSet<>();

    public void regS11nType(Class type) {
        serializeTypes.add(type);
    }

    public boolean isS11nType(Class type) {
        return type.isEnum() ||
                type.isAnnotationPresent(SerializeType.class) ||
                serializeTypes.contains(type);
    }

    private void reg(Class... type) {
        for(Class c : type) {
            regS11nType(c);
        }
    }

    {
        reg(char.class, Character.class,
                byte.class, Byte.class,
                short.class, Short.class,
                int.class, Integer.class,
                float.class, Float.class,
                double.class, Double.class,
                boolean.class, Boolean.class,
                String.class,
                Vec3.class,
                ResourceLocation.class);
    }

    /**
     * Get the fields exposed in recursive serialization for the given type.
     */
    public List<Field> getExposedFields(Class<?> type) {
        return FieldUtils.getAllFieldsList(type)
                .stream()
                .filter(f -> {
                    Class<?> declaringClass = f.getDeclaringClass();
                    SerializeStrategy anno = declaringClass.getAnnotation(SerializeStrategy.class);
                    ExposeStrategy strategy = anno == null ? ExposeStrategy.PUBLIC : anno.strategy();
                    boolean serializeAll = anno == null ? false : anno.all();

                    if (f.isAnnotationPresent(SerializeIncluded.class)) {
                        return true;
                    } else if (f.isAnnotationPresent(SerializeExcluded.class)) {
                        return false;
                    } else {
                        if (!serializeAll && !isS11nType(f.getType())) {
                            return false;
                        } else {
                            int mod = f.getModifiers();
                            switch (strategy) {
                            case PUBLIC:
                                return Modifier.isPublic(mod) && !Modifier.isStatic(mod) && !Modifier.isFinal(mod);
                            case ALL:
                                return !Modifier.isStatic(mod) && !Modifier.isFinal(mod);
                            default:
                                return false;
                            }
                        }
                    }
                })
                .map(f -> {
                    f.setAccessible(true);
                    return f;
                })
                .collect(Collectors.toList());
    }

    public SerializationHelper() {}

}
