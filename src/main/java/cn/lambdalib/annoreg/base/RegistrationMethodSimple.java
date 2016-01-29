/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import cn.lambdalib.annoreg.core.AnnotationData;
import cn.lambdalib.annoreg.core.RegistryType;

/**
 * @author WeAthFolD
 */
public abstract class RegistrationMethodSimple<ANNO extends Annotation> extends RegistryType {

    public RegistrationMethodSimple(Class<? extends ANNO> annoClass, String name) {
        super(annoClass, name);
    }
    
    protected abstract void register(Method method, ANNO value) throws Exception;
    
    @Override
    public boolean registerMethod(AnnotationData data) throws Exception {
        register(data.getTheMethod(), data.getAnnotation());
        return true;
    }

    @Override
    public boolean registerClass(AnnotationData data) throws Exception {
        throw new RuntimeException("Invalid use of annotation " + this.annoClass.getSimpleName() + ": Can not use on class.");
    }

    @Override
    public boolean registerField(AnnotationData data) throws Exception {
        throw new RuntimeException("Invalid use of annotation " + this.annoClass.getSimpleName() + ": Can not use on field.");
    }

}
