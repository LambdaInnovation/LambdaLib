/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
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
