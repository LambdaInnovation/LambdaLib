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
package cn.annoreg.base;

import java.lang.annotation.Annotation;

import cn.annoreg.ARModContainer;
import cn.annoreg.core.AnnotationData;
import cn.annoreg.core.RegModInformation;
import cn.annoreg.core.RegistrationWithPostWork;
import cn.annoreg.core.RegistryType;

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
}
