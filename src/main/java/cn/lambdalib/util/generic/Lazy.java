package cn.lambdalib.util.generic;

import java.util.function.Supplier;

/**
 * A generic lazy-initialized object.
 */
public class Lazy<T> {

    public static <T> Supplier<T> withInitializer(Supplier<T> initializer) {
        return new Supplier<T>() {
            boolean initialized;
            T value;

            @Override
            public T get() {
                if (!initialized) {
                    value = initializer.get();
                    initialized = true;
                }

                return value;
            }
        };
    }

    private T value;
    private boolean initialized;

    public T get(Supplier<T> initializer) {
        if (!initialized) {
            value = initializer.get();
            initialized = true;
        }
        return value;
    }

}
