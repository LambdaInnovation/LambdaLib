/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import cn.lambdalib.annoreg.core.AnnotationData;
import cn.lambdalib.annoreg.core.RegistryType;

public class RegistrationEmpty extends RegistryType {

    public RegistrationEmpty(String name) {
        super(name);
    }

    @Override
    public boolean registerClass(AnnotationData data) throws Exception {
        return false;
    }

    @Override
    public boolean registerField(AnnotationData data) throws Exception {
        return false;
    }
    
    @Override
    public boolean registerMethod(AnnotationData data) throws Exception {
        return false;
    }

    @Override
    public void visitClass(Class<?> clazz) {}
    
    @Override
    public void visitField(Field field) {}
    
}
