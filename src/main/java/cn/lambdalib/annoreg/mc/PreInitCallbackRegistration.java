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
public class PreInitCallbackRegistration extends RegistrationMethodSimple<RegPreInitCallback> {

    public PreInitCallbackRegistration() {
        super(RegPreInitCallback.class, "PreInitCallback");
        setLoadStage(LoadStage.PRE_INIT);
    }

    @Override
    protected void register(Method method, RegPreInitCallback value) throws Exception {
        method.invoke(null);
    }

}