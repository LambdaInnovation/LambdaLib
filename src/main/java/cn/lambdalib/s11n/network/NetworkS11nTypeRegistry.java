/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.s11n.network;

import cn.lambdalib.annoreg.core.AnnotationData;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryType;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.s11n.network.NetworkS11n.NetworkS11nType;

@RegistryTypeDecl
public class NetworkS11nTypeRegistry extends RegistryType {

    public NetworkS11nTypeRegistry() {
        super(NetworkS11nType.class, "NetworkS11nType");
        setLoadStage(LoadStage.PRE_INIT);
    }

    @Override
    public boolean registerClass(AnnotationData data) throws Exception {
        NetworkS11n.register(data.getTheClass());
        return true;
    }

}
