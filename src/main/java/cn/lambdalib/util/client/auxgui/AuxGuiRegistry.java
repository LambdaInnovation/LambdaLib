/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client.auxgui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.lambdalib.annoreg.base.RegistrationInstance;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.core.LambdaLib;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * AuxGui register annotation.
 * @author WeathFolD
 */
@RegistryTypeDecl
@SideOnly(Side.CLIENT)
public class AuxGuiRegistry extends RegistrationInstance<AuxGuiRegistry.RegAuxGui, AuxGui> {
    
    @Target({ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @SideOnly(Side.CLIENT)
    public @interface RegAuxGui {}

    public AuxGuiRegistry() {
        super(RegAuxGui.class, "AuxGui");
        this.setLoadStage(LoadStage.INIT);
    }

    @Override
    protected void register(AuxGui obj, RegAuxGui anno) throws Exception {
        AuxGui.register(obj);
    }

}
