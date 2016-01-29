/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

import cn.lambdalib.annoreg.base.RegistrationMethodSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;

import java.lang.reflect.Method;

@RegistryTypeDecl
public class PostInitCallbackRegistration extends RegistrationMethodSimple<RegPostInitCallback> {

    public PostInitCallbackRegistration() {
        super(RegPostInitCallback.class, "PostInitCallback");
        setLoadStage(LoadStage.POST_INIT);
    }

    @Override
    protected void register(Method method, RegPostInitCallback value) throws Exception {
        method.invoke(null);
    }

}
