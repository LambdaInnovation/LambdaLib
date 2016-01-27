/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AnnotationData {
    public enum Type {
        CLASS, FIELD, METHOD
    }
    
    public final Type type;
    public final Annotation anno;
    public final Object reflect;
    public RegModInformation mod;
    
    public AnnotationData(Annotation anno, Class<?> clazz) {
        this.anno = anno;
        this.reflect = clazz;
        this.type = Type.CLASS;
    }
    
    public AnnotationData(Annotation anno, Field field) {
        this.anno = anno;
        this.reflect = field;
        this.type = Type.FIELD;
    }
    
    public AnnotationData(Annotation anno, Method method) {
        this.anno = anno;
        this.reflect = method;
        this.type = Type.METHOD;
    }

    public <T extends Annotation> T getAnnotation() {
        return (T) anno;
    }
    
    public boolean isClass() {
        return type == Type.CLASS;
    }
    
    public boolean isField() {
        return type == Type.FIELD;
    }
    
    public boolean isMethod() {
        return type == Type.METHOD;
    }
    
    public Class<?> getTheClass() {
        return isClass() ? (Class<?>) reflect : null;
    }
    
    public Field getTheField() {
        return isField() ? (Field) reflect : null;
    }
    
    public Method getTheMethod() {
        return isMethod() ? (Method) reflect : null;
    }
    
    @Override
    public String toString() {
        return "AnnotationData (" + anno.annotationType().getSimpleName() + ", " +
                (isClass() ? getTheClass().getCanonicalName() :
                        isMethod() ? getTheMethod().toString() : getTheField().toString()) + ")";
    }
}
