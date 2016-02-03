/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.core;

import cpw.mods.fml.common.event.*;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import cn.lambdalib.annoreg.core.RegistrationManager;
import cn.lambdalib.annoreg.core.RegistrationMod;
import cn.lambdalib.annoreg.mc.RegMessageHandler;
import cn.lambdalib.core.command.CmdMineStatistics;
import cn.lambdalib.util.deprecated.LIFMLGameEventDispatcher;
import cn.lambdalib.util.reschk.ResourceCheck;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraft.command.CommandHandler;

@Mod(modid = "LambdaLib", name = "LambdaLib", version = LambdaLib.VERSION, dependencies = "required-after:"
        + LLModContainer.MODID)
@RegistrationMod(pkg = "cn.lambdalib.", res = "lambdalib", prefix = "ll_")
public class LambdaLib {

    public static final String VERSION = "1.1.1";

    /**
     * Does open debug mode. turn to false when compiling.
     */
    public static final boolean DEBUG = false;

    public static final Logger log = FMLLog.getLogger();

    private static Configuration config;

    @RegMessageHandler.WrapperInstance
    public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel("LambdaLib");

    public static Configuration getConfig() {
        return config;
    }

    @EventHandler()
    public void preInit(FMLPreInitializationEvent event) {
        log.info("Starting LambdaLib");
        log.info("Copyright (c) Lambda Innovation, 2013-2015");
        log.info("http://www.li-dev.cn/");

        ResourceCheck.init();
        LIFMLGameEventDispatcher.init();

        config = new Configuration(event.getSuggestedConfigurationFile());

        RegistrationManager.INSTANCE.registerAll(this, "PreInit");
    }

    @EventHandler()
    public void init(FMLInitializationEvent event) {
        RegistrationManager.INSTANCE.registerAll(this, "Init");
    }

    @EventHandler()
    public void postInit(FMLPostInitializationEvent event) {
        RegistrationManager.INSTANCE.registerAll(this, "PostInit");
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        config.save();
    }

    @EventHandler()
    public void serverStarting(FMLServerStartingEvent event) {
        CommandHandler cm = (CommandHandler) event.getServer().getCommandManager();
        if (DEBUG) {
            cm.registerCommand(new CmdMineStatistics());
        }
        RegistrationManager.INSTANCE.registerAll(this, "StartServer");
    }

}
