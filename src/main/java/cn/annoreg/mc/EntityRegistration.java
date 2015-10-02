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
package cn.annoreg.mc;

import cn.annoreg.ARModContainer;
import cn.annoreg.base.RegistrationClassSimple;
import cn.annoreg.core.LoadStage;
import cn.annoreg.core.RegistryTypeDecl;
import cn.annoreg.mc.RegEntity.HasRender;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.Entity;

@RegistryTypeDecl
public class EntityRegistration extends RegistrationClassSimple<RegEntity, Entity> {

	public EntityRegistration() {
		super(RegEntity.class, "Entity");
		this.setLoadStage(LoadStage.INIT);
		
		this.addWork(RegEntity.HasRender.class, new PostWork<RegEntity.HasRender, Class<? extends Entity>>() {
			@Override
			public void invoke(HasRender anno, Class<? extends Entity> obj) throws Exception {
				if (ProxyHelper.isClient()) {
					ProxyHelper.regEntityRender(obj, helper.getFieldFromClass(obj, RegEntity.Render.class));
					//System.out.println("[AR]Registered render " + helper.getFieldFromClass(obj, RegEntity.Render.class) + "for " + obj);
				}
			}
		});
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
