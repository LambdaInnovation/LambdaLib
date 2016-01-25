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

import cn.lambdalib.annoreg.base.RegistrationClassSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.annoreg.mc.RegEntity.HasRender;
import cn.lambdalib.util.mc.SideHelper;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
