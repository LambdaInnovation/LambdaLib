/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.networkcall.s11n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegSerializable {
    
    /**
     * Custom instance serializer.
     * @return
     */
    Class<? extends InstanceSerializer> instance() default InstanceSerializer.class;
    /**
     * Custom data serializer. If you want auto serialization (with @SerializedField),
     * keep it as default.
     * @return
     */
    Class<? extends DataSerializer> data() default DataSerializer.class;

    /**
     * Used to help to generate an auto serializer.
     * Now you can only use DATA option to serialize a field.
     * @author acaly
     *
     */
    //TODO add instance serialization support for field.
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SerializeField {}
    
}
