package cn.lambdalib.s11n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation alternates the serialization strategy of recursive type.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializeType {

    enum ExposeStrategy {
        /** Just public fields */
        PUBLIC,
        /** Includes private fields */
        ALL
    }

    /**
     * @return How fields of this recursive type are exposed.
     */
    ExposeStrategy stragety() default ExposeStrategy.PUBLIC;

}
