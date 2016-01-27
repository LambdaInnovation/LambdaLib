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
import cn.lambdalib.annoreg.core.RegistrationWithPostWork;
import cn.lambdalib.annoreg.core.RegistryType;
import cn.lambdalib.core.LLModContainer;

/**
 * Register a class. Do post work with the class.
 * @author acaly
 *
 * @param <ANNO>
 * @param <BASE>
 */
public abstract class RegistrationClassSimple<ANNO extends Annotation, BASE> 
        extends RegistrationWithPostWork<Class<? extends BASE>> {

    public RegistrationClassSimple(Class<ANNO> annoClass, String name) {
        super(annoClass, name);
    }

    protected abstract void register(Class<? extends BASE> theClass, ANNO anno) throws Exception;

    @Override
    public boolean registerClass(AnnotationData data) throws Exception {
        Class<? extends BASE> clazz = (Class<? extends BASE>) data.getTheClass();
        register(clazz, (ANNO) data.getAnnotation());
        this.doPostRegWork(clazz, clazz);
        return true;
    }

    @Override
    public boolean registerField(AnnotationData data) throws Exception {
        throw new RuntimeException("Invalid use of annotation " + this.annoClass.getSimpleName() + ": Can not use on field.");
    }
    
    @Override
    public boolean registerMethod(AnnotationData data) throws Exception {
        throw new RuntimeException("Invalid use of annotation " + this.annoClass.getSimpleName() + ": Can not use on method.");
    }
}
