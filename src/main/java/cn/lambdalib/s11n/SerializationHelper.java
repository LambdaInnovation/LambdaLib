package cn.lambdalib.s11n;

import cn.lambdalib.s11n.SerializeType.ExposeStrategy;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SerializationHelper {

    /**
     * Get the fields exposed in recursive serialization for the given type.
     */
    public static List<Field> getExposedFields(Class<?> type) {
        SerializeType anno = type.getAnnotation(SerializeType.class);
        ExposeStrategy strategy = anno == null ? ExposeStrategy.PUBLIC : anno.stragety();

        return FieldUtils.getAllFieldsList(type)
                .stream()
                .filter(f -> {
                    if (f.isAnnotationPresent(SerializeIncluded.class)) {
                        return true;
                    } else if (f.isAnnotationPresent(SerializeExcluded.class)) {
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
                })
                .map(f -> {
                    f.setAccessible(true);
                    return f;
                })
                .collect(Collectors.toList());
    }

    private SerializationHelper() {}

}
