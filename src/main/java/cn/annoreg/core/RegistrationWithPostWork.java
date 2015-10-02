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
package cn.annoreg.core;

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
