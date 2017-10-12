/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc.gui;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lambdalib.annoreg.base.RegistrationFieldSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegModInformation;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.core.LLModContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@RegistryTypeDecl
public class GuiHandlerRegistration extends RegistrationFieldSimple<RegGuiHandler, GuiHandlerBase> {

    public GuiHandlerRegistration() {
        super(RegGuiHandler.class, "GuiHandler");
        this.setLoadStage(LoadStage.INIT);
    }

    private static class ModGuiHandler implements IGuiHandler
    {

        private List<IGuiHandler> subHandlers = new ArrayList();
        
        public int addHandler(IGuiHandler handler) {
            subHandlers.add(handler);
            return subHandlers.size() - 1;
        }
        
        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            if (ID >= subHandlers.size()) {
                LLModContainer.log.error("Invalid GUI id on server.");
                return null;
            }
            return subHandlers.get(ID).getServerGuiElement(0, player, world, x, y, z);
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
            if (ID >= subHandlers.size()) {
                LLModContainer.log.error("Invalid GUI id on client.");
                return null;
            }
            return subHandlers.get(ID).getClientGuiElement(0, player, world, x, y, z);
        }
        
    }
    
    private Map<RegModInformation, ModGuiHandler> modHandlers = new HashMap();
    
    private void regHandler(RegModInformation mod, GuiHandlerBase handler) {
        ModGuiHandler modHandler = modHandlers.get(mod);
        if (modHandler == null) {
            modHandler = new ModGuiHandler();
            modHandlers.put(mod, modHandler);
            NetworkRegistry.INSTANCE.registerGuiHandler(mod.getModInstance(), modHandler);
        }
        int id =  modHandler.addHandler(handler.getHandler());
        handler.register(mod.getModInstance(), id);
    }

    @Override
    protected void register(GuiHandlerBase value, RegGuiHandler anno, String field) throws Exception {
        regHandler(this.getCurrentMod(), value);
    }

}
