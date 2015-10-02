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
package cn.liutils.api;

import java.lang.reflect.Field;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import cn.liutils.api.register.Configurable;

/**
 * A public interface for all LIUtils Non-Client API functions.
 * @author WeAthFolD
 */
public class LIGeneralRegistry {
	
	/**
	 * Initialize a class's #cn.liutils.api.register.Configurable fields.
	 * @param conf The data source(config)
	 * @param cl The class containg property variables. currently they must be static.
	 * @see cn.liutils.api.register.Configurable
	 */
	public static void loadConfigurableClass(Configuration conf, Class<?> cl) {
		Property prop;
		for (Field f : cl.getFields()) {
			Configurable c = f.getAnnotation(Configurable.class);
			if (c != null) {
				try {
					Class<?> type = f.getType();
					if (type.equals(Integer.TYPE) || type.equals(Integer.class)) {
						prop = conf.get(c.category(), c.key(), c.defValueInt());
						prop.comment = c.comment();
						f.setInt(null, prop.getInt(c.defValueInt()));
					} else if (type.equals(Boolean.TYPE) || type.equals(Boolean.class)) {
						prop = conf.get(c.category(), c.key(), c.defValueBool());
						prop.comment = c.comment();
						f.setBoolean(null, prop.getBoolean(c.defValueBool()));
					} else if (type.equals(Double.TYPE) || type.equals(Double.class)) {
						prop = conf.get(c.category(), c.key(), c.defValueDouble());
						prop.comment = c.comment();
						f.setDouble(null, prop.getDouble(c.defValueDouble()));
					} else if (type.equals(String.class)) {
						prop = conf.get(c.category(), c.key(), c.defValue());
						prop.comment = c.comment();
						f.set(null, prop.getString());
					} else {
						throw new UnsupportedOperationException();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void storeConfigurableClass(Configuration conf, Class<?> cl) {
		Property prop;
		for (Field f : cl.getFields()) {
			Configurable c = f.getAnnotation(Configurable.class);
			if (c != null) {
				try {
					prop = conf.get(c.category(), c.key(), c.defValue());
					prop.comment = c.comment();
					prop.set(String.valueOf(f.get(null)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		conf.save();
	}

}
