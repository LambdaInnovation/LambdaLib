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
package cn.lambdalib.annoreg.mc;

import net.minecraft.tileentity.TileEntity;
import cn.lambdalib.annoreg.base.RegistrationClassSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.annoreg.mc.RegTileEntity.HasRender;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@RegistryTypeDecl
public class TileEntityRegistration extends RegistrationClassSimple<RegTileEntity, TileEntity> {

    public TileEntityRegistration() {
        super(RegTileEntity.class, "TileEntity");
        this.setLoadStage(LoadStage.INIT);
        
        if(FMLCommonHandler.instance().getSide() == Side.CLIENT)
        this.addWork(HasRender.class, new PostWork<HasRender, Class<? extends TileEntity>>() {
            @Override
            public void invoke(HasRender anno, Class<? extends TileEntity> obj) throws Exception {
                ProxyHelper.regTileEntityRender(obj, helper.getFieldFromClass(obj, RegTileEntity.Render.class));
            }
        });
    }

    @Override
    protected void register(Class<? extends TileEntity> theClass, RegTileEntity anno) throws Exception {
        GameRegistry.registerTileEntity(theClass, getSuggestedName());
    }
}
