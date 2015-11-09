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
				(isClass() ? getTheClass().getCanonicalName() : getTheField().toString()) + ")";
	}
}
