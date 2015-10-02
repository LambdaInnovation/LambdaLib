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
package cn.annoreg.mc.s11n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegSerializable {
	
    /**
     * Custom instance serializer.
     * @return
     */
	Class<? extends InstanceSerializer> instance() default InstanceSerializer.class;
	/**
	 * Custom data serializer. If you want auto serialization (with @SerializedField),
	 * keep it as default.
	 * @return
	 */
	Class<? extends DataSerializer> data() default DataSerializer.class;

	/**
	 * Used to help to generate an auto serializer.
	 * Now you can only use DATA option to serialize a field.
	 * @author acaly
	 *
	 */
	//TODO add instance serialization support for field.
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface SerializeField {}
	
}
