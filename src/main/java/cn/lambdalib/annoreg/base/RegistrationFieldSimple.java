/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
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
