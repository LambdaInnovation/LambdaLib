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
package cn.liutils.util.helper;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author WeAthFolD
 */
public abstract class HandledType {
	
	public void set(Field f, Object instance, Object value) throws Exception {
		f.set(instance, value);
	}
	
	public abstract void edit(Field f, Object instance, String value) throws Exception;
	
	public String repr(Field f, Object instance) throws Exception {
		return f.get(instance).toString();
	}
	
	public Object get(Field f, Object instance) throws Exception {
		return f.get(instance);
	}
	
	public abstract Object copy(Field f, Object instance) throws Exception;
	
}
