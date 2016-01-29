/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import cn.lambdalib.annoreg.core.AnnotationData;
import cn.lambdalib.annoreg.core.RegModInformation;
import cn.lambdalib.annoreg.core.RegistrationWithPostWork;
import cn.lambdalib.core.LLModContainer;

public abstract class RegistrationFieldSimple<ANNO extends Annotation, BASE> extends RegistrationWithPostWork<BASE> {

    public RegistrationFieldSimple(Class<ANNO> annoClass, String name) {
        super(annoClass, name);
    }
    
    protected abstract void register(BASE value, ANNO anno, String field) throws Exception;

    @Override
    public boolean registerClass(AnnotationData data) throws Exception {
        throw new RuntimeException("Invalid use of annotation " + this.annoClass.getSimpleName() + ": Can not use on class.");
    }
    
    @Override
    public boolean registerMethod(AnnotationData data) throws Exception {
        throw new RuntimeException("Invalid use of annotation " + this.annoClass.getSimpleName() + ": Can not use on method.");
    }

    @Override
    public boolean registerField(AnnotationData data) throws Exception {
        Field field = data.getTheField();
        if (!Modifier.isStatic(field.getModifiers())) {
            LLModContainer.log.error("Invalid use of annotation {}: Field must be static.",
                    this.annoClass.getSimpleName());
        }
        BASE value = null;
        try {
            value = (BASE) field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Can not get the value of the field " + field.toString() + ".", e);
        }
        try {
            if (value == null) {
                // Create new instance. Use default constructor.
                value = (BASE) field.getType().newInstance();
                field.set(null, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Can not create new instance for the field.", e);
        }
        register(value, (ANNO) data.getAnnotation(), field.getName());
        doPostRegWork(field, value);
        return true;
    }

}
