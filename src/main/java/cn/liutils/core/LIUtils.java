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
package cn.liutils.core;

import net.minecraft.command.CommandHandler;
import net.minecraft.entity.Entity;

import org.apache.logging.log4j.Logger;

import cn.annoreg.core.RegistrationManager;
import cn.annoreg.core.RegistrationMod;
import cn.annoreg.mc.RegMessageHandler;
import cn.liutils.check.ResourceCheck;
import cn.liutils.core.event.eventhandler.LIFMLGameEventDispatcher;
import cn.liutils.debug.CmdMineStatistics;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;

@Mod(modid = "LIUtils", name = "LIUtils", version = LIUtils.VERSION, dependencies="required-after:AnnoReg")
@RegistrationMod(pkg = "cn.liutils.", res = "liutils", prefix = "liu_")
public class LIUtils {
	
	public static final String
		REGISTER_TYPE_KEYHANDLER = "liu_akhs",
		REGISTER_TYPE_KEYHANDLER2 = "liu_khs",
		REGISTER_TYPE_AUXGUI = "liu_auxgui",
		REGISTER_TYPE_RENDER_HOOK = "liu_playerhook",
		REGISTER_TYPE_CONFIGURABLE = "liu_configurable";
	
	public static final String VERSION = "2.0";
	
	/**
	 * Does open debug mode. turn to false when compiling.
	 */
	public static final boolean DEBUG = false;
	
	@Instance("LIutils")
	public static LIUtils instance;
	
	public static Logger log = FMLLog.getLogger();

	@RegMessageHandler.WrapperInstance
	public static SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel("LIUtils");
	
	@EventHandler()
	public void preInit(FMLPreInitializationEvent event) {
		
		log.info("Starting LIUtils");
		log.info("Copyright (c) Lambda Innovation, 2013-2015");
		log.info("http://www.li-dev.cn/");
		
		ResourceCheck.init();
		LIFMLGameEventDispatcher.init();
		
		RegistrationManager.INSTANCE.registerAll(this, "PreInit");
	}
	
	@EventHandler()
	public void init(FMLInitializationEvent Init) {
		RegistrationManager.INSTANCE.registerAll(this, "Init");
	}
	
	@EventHandler()
	public void postInit(FMLPostInitializationEvent event) {
		RegistrationManager.INSTANCE.registerAll(this, "PostInit");
	}
	
	@EventHandler()
	public void serverStarting(FMLServerStartingEvent event) {
		CommandHandler cm = (CommandHandler) event.getServer().getCommandManager();
		if(DEBUG) {
		    cm.registerCommand(new CmdMineStatistics());
		}
		RegistrationManager.INSTANCE.registerAll(this, "StartServer");
	}
	
	private void registerEntity(Class<? extends Entity> cl, String name, int id) {
		registerEntity(cl, name, id, 32, 3, true);
	}
	
	private void registerEntity(Class<? extends Entity> cl, String name, int id, int trackRange, int freq, boolean updateVel) {
		EntityRegistry.registerModEntity(cl, name, id, instance, trackRange, freq, updateVel);
	}
	
}
