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
import cn.lambdalib.s11n.network.NetworkS11n.NetS11nAdaptor;

import java.lang.annotation.*;
import java.util.Objects;

@RegistryTypeDecl
public class NetS11nAdapterRegistry extends RegistryType {

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegNetS11nAdapter {
        Class value();
    }

    public NetS11nAdapterRegistry() {
        super(RegNetS11nAdapter.class, "NetS11nAdapter");
        setLoadStage(LoadStage.PRE_INIT);
    }

    @Override
    public boolean registerField(AnnotationData data) throws Exception {
        NetS11nAdaptor adaptor = (NetS11nAdaptor) data.getTheField().get(null);
        Objects.requireNonNull(adaptor);
        NetworkS11n.addDirect(data.<RegNetS11nAdapter>getAnnotation().value(), adaptor);
        return true;
    }
}
