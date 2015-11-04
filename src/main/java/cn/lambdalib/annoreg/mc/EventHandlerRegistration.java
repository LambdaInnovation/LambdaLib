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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import cn.lambdalib.annoreg.base.RegistrationInstance;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegModInformation;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventBus;

@RegistryTypeDecl
public class EventHandlerRegistration extends RegistrationInstance<RegEventHandler, Object> {

	public EventHandlerRegistration() {
		super(RegEventHandler.class, "EventHandler");
		this.setLoadStage(LoadStage.INIT);
	}
	
	@Override
	protected void register(Object obj, RegEventHandler anno) throws Exception {
		for (RegEventHandler.Bus bus : anno.value()) {
			switch (bus) {
			case FML:
				FMLCommonHandler.instance().bus().register(obj);
				break;
			case Forge:
				MinecraftForge.EVENT_BUS.register(obj);
				break;
			default:
			}
		}
	}
	
}
