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
