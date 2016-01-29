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
import cn.lambdalib.annoreg.core.RegistryType;
import cn.lambdalib.core.LLModContainer;

public abstract class RegistrationClassRepeat<ANNO extends Annotation, BASE> extends RegistryType {

    public RegistrationClassRepeat(Class<ANNO> annoClass, String name) {
        super(annoClass, name);
    }

    protected abstract void register(Class<? extends BASE> theClass, ANNO anno) throws Exception;

    @Override
    public boolean registerClass(AnnotationData data) throws Exception {
        register((Class<? extends BASE>) data.getTheClass(), (ANNO) data.getAnnotation());
        return false;
    }

    @Override
    public boolean registerField(AnnotationData data) throws Exception {
        throw new RuntimeException("Invalid use of annotation " + this.annoClass.getSimpleName() + ": Can not use on field.");
    }
    
    @Override
    public boolean registerMethod(AnnotationData data) throws Exception {
        throw new RuntimeException("Invalid use of annotation " + this.annoClass.getSimpleName() + ": Can not use on method.");
    }

    @Override
    public void checkLoadState() {
    }
}
