/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.key;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.lambdalib.annoreg.base.RegistrationFieldSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.util.key.KeyHandlerRegistration.RegKeyHandler;

/**
 * @author WeAthFolD
 */
@RegistryTypeDecl
public class KeyHandlerRegistration extends RegistrationFieldSimple<RegKeyHandler, KeyHandler> {
    
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegKeyHandler {
        String name();
        int keyID();
    }

    public KeyHandlerRegistration() {
        super(RegKeyHandler.class, "KeyHandler");
        setLoadStage(LoadStage.INIT);
    }

    @Override
    protected void register(KeyHandler value, RegKeyHandler anno, String field)
            throws Exception {
        KeyManager.dynamic.addKeyHandler(anno.name(), anno.keyID(), value);
    }
    
    
    
}
