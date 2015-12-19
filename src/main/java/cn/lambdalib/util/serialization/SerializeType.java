package cn.lambdalib.util.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a type to be able to be auto-serialized. When serializing an object, only all fields of serializable types
 * 	within the object class can be serialized.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializeType {
}
