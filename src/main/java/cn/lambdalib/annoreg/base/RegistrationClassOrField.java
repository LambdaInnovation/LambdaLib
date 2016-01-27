/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.base;

import java.lang.annotation.Annotation;

import cn.lambdalib.annoreg.core.AnnotationData;
import cn.lambdalib.annoreg.core.RegModInformation;

public abstract class RegistrationClassOrField<ANNO extends Annotation> extends RegistrationFieldSimple<ANNO, Object> {

    public RegistrationClassOrField(Class<ANNO> annoClass, String name) {
        super(annoClass, name);
    }
    
    protected abstract void register(Class<?> value, ANNO anno) throws Exception;

    @Override
    public boolean registerClass(AnnotationData data) throws Exception {
        register(data.getTheClass(), (ANNO) data.anno);
        return true;
    }
}
