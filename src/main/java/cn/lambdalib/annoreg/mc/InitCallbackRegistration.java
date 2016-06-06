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
import cn.lambdalib.core.LambdaLib;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author WeAthFolD
 */
@RegistryTypeDecl
public class InitCallbackRegistration extends RegistrationMethodSimple<RegInitCallback> {

    public InitCallbackRegistration() {
        super(RegInitCallback.class, "InitCallback");
        setLoadStage(LoadStage.INIT);
    }

    @Override
    protected void register(Method method, RegInitCallback value) throws Exception {
        if (!Modifier.isPrivate(method.getModifiers())) {
            LambdaLib.log.warn("Init method " + method + " is not private.");
        }
        method.invoke(null);
    }
    
}
