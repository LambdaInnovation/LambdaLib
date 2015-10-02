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

import cn.annoreg.base.RegistrationClassSimple;
import cn.annoreg.core.LoadStage;
import cn.annoreg.core.RegistryTypeDecl;

@RegistryTypeDecl
public class SerializableRegistration extends RegistrationClassSimple<RegSerializable, Object> {

	public SerializableRegistration() {
		super(RegSerializable.class, "Serializable");
		this.setLoadStage(LoadStage.INIT);
	}

	@Override
	protected void register(Class<? extends Object> theClass, RegSerializable anno) throws Exception {
		if (SerializationManager.INSTANCE.getInstanceSerializer(theClass) == null) {
			if (!anno.instance().equals(InstanceSerializer.class)) {
				SerializationManager.INSTANCE.setInstanceSerializerFor(theClass, anno.instance().newInstance());
			}
		}
		if (!SerializationManager.INSTANCE.hasDataSerializer(theClass)) {
			if (anno.data().equals(DataSerializer.class)) {
				SerializationManager.INSTANCE.setDataSerializerFor(theClass, 
						SerializationManager.INSTANCE.createAutoSerializerFor(theClass));
			} else {
				SerializationManager.INSTANCE.setDataSerializerFor(theClass, anno.data().newInstance());
			}
		}
	}

}
