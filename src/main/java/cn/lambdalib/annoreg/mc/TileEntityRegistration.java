/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

import cn.lambdalib.util.mc.SideHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import cn.lambdalib.annoreg.base.RegistrationClassSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.annoreg.mc.RegTileEntity.HasRender;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@RegistryTypeDecl
public class TileEntityRegistration extends RegistrationClassSimple<RegTileEntity, TileEntity> {

    public TileEntityRegistration() {
        super(RegTileEntity.class, "TileEntity");
        this.setLoadStage(LoadStage.INIT);
        
        if(SideHelper.isClient()) {
            this.addWork(HasRender.class, new PostWork<HasRender, Class<? extends TileEntity>>() {
                @Override
                @SideOnly(Side.CLIENT)
                public void invoke(HasRender anno, Class<? extends TileEntity> obj) throws Exception {
                    ClientRegistry.bindTileEntitySpecialRenderer(obj,
                            (TileEntitySpecialRenderer) helper.getFieldFromClass(obj, RegTileEntity.Render.class));
                }
            });
        }
    }

    @Override
    protected void register(Class<? extends TileEntity> theClass, RegTileEntity anno) throws Exception {
        GameRegistry.registerTileEntity(theClass, getSuggestedName());
    }
}
