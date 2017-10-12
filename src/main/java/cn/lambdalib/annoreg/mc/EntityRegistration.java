/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

import cn.lambdalib.annoreg.base.RegistrationClassSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.annoreg.mc.RegEntity.HasRender;
import cn.lambdalib.util.mc.SideHelper;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

@RegistryTypeDecl
public class EntityRegistration extends RegistrationClassSimple<RegEntity, Entity> {

    public EntityRegistration() {
        super(RegEntity.class, "Entity");
        this.setLoadStage(LoadStage.INIT);

        if (SideHelper.isClient()) {
            this.addWork(RegEntity.HasRender.class, new PostWork<RegEntity.HasRender, Class<? extends Entity>>() {
                @SideOnly(Side.CLIENT)
                @Override
                public void invoke(HasRender anno, Class<? extends Entity> obj) throws Exception {
                    RenderingRegistry.registerEntityRenderingHandler(obj,
                            (Render) helper.getFieldFromClass(obj, RegEntity.Render.class));
                }
            });
        }
    }
    
    @Override
    protected void register(Class<? extends Entity> theClass, RegEntity anno) throws Exception {
        if (!anno.clientOnly()) {
            EntityRegistry.registerModEntity(theClass, getSuggestedName(),
                    helper.getNextIDForMod(), getCurrentMod().getModInstance(), 
                    anno.trackRange(), anno.freq(), anno.updateVel());
        }
    }
}
