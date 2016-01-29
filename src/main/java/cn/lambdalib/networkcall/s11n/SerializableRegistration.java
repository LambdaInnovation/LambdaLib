/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.networkcall.s11n;

import cn.lambdalib.annoreg.base.RegistrationClassSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;

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
