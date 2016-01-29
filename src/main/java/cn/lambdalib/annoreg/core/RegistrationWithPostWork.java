/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements doPostRegWork, which is very useful for other RegistryTypes.
 * @author acaly
 *
 */
public abstract class RegistrationWithPostWork<OBJ> extends RegistryType {
    
    public interface PostWork<T extends Annotation, OBJ> {
        void invoke(T anno, OBJ obj) throws Exception;
    }

    public RegistrationWithPostWork(Class<? extends Annotation> annoClass, String name) {
        super(annoClass, name);
    }
    
    private static class Work {
        public final Class<? extends Annotation> anno;
        public final PostWork func;
        public Work(Class<? extends Annotation> anno, PostWork func) {
            this.anno = anno;
            this.func = func;
        }
    }
    
    private Set<Work> works = new HashSet();

    protected void addWork(Class<? extends Annotation> anno, PostWork<? extends Annotation, OBJ> func) {
        works.add(new Work(anno, func));
    }
    
    protected void doPostRegWork(Field field, OBJ obj) throws Exception {
        for (Work work : works) {
            if (field.isAnnotationPresent(work.anno)) {
                work.func.invoke(field.getAnnotation(work.anno), obj);
            }
        }
    }
    
    protected void doPostRegWork(Class<?> clazz, OBJ obj) throws Exception {
        for (Work work : works) {
            if (clazz.isAnnotationPresent(work.anno)) {
                work.func.invoke(clazz.getAnnotation(work.anno), obj);
            }
        }
    }
}
