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
package cn.lambdalib.core;

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
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraft.command.CommandHandler;

@Mod(modid = "LambdaLib", name = "LambdaLib", version = LambdaLib.VERSION, dependencies="required-after:" + LLModContainer.MODID)
@RegistrationMod(pkg = "cn.lambdalib.", res = "lambdalib", prefix = "ll_")
public class LambdaLib {
	
	public static final String VERSION = "1.0";
	
	/**
	 * Does open debug mode. turn to false when compiling.
	 */
	public static final boolean DEBUG = true;
	
	public static Logger log = FMLLog.getLogger();

	@RegMessageHandler.WrapperInstance
	public static SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel("LambdaLib");
	
	@EventHandler()
	public void preInit(FMLPreInitializationEvent event) {
		log.info("Starting LambdaLib");
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
	
}
