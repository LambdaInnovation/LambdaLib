package cn.lambdalib.util.serialization;

import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SerializationHelper {

	private Set<Class<?>> serializeTypes = new HashSet<>();

	// API

	/**
	 * Get the fields exposed by the rules of this SerializationHelper.
	 */
	public List<Field> getExposedFields(Class<?> type) {
		return Arrays.stream(type.getFields())
				.filter(f -> {
					int modifiers = f.getModifiers();
					boolean canSerialize = (modifiers & (Modifier.FINAL | Modifier.STATIC)) == 0 &&
							!f.isAnnotationPresent(SerializeExcluded.class) && isSerializable(f.getType());
					return canSerialize && ((modifiers & Modifier.PUBLIC) != 0 ||
							f.isAnnotationPresent(SerializeIncluded.class));
				})
				.collect(Collectors.toList());
	}

	public boolean isSerializable(Class<?> type) {
		return type.isEnum() ||
				type.isAnnotationPresent(SerializeType.class) ||
				serializeTypes.contains(type);
	}
	//

	// Behaviour alternation
	public void addSerializedType(Class<?> type) {
		serializeTypes.add(type);
	}
	//

}
