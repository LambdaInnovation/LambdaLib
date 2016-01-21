package cn.lambdalib.s11n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation marks a type as serializable. Only fields with type marked as serializable and types as stated in
 *  {@link SerializationHelper} will be automatically serialized in recursive serialization.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializeType {
}
