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
