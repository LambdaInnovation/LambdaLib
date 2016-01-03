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
package cn.lambdalib.util.helper;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import cn.lambdalib.annoreg.core.Registrant;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * A simple timer wrapup to handle paused timing situations.
 * @author WeAthFolD
 */
public enum GameTimer {
    INSTANCE;
    
    GameTimer() {
        FMLCommonHandler.instance().bus().register(this);
    }
    
    static long storedTime, timeLag;
    
    public static long getTime() {
        return FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT ? getTimeClient() : getTimeServer();
    }
    
    public static long getAbsTime() {
        return MinecraftServer.getSystemTimeMillis();
    }
    
    @SideOnly(Side.CLIENT)
    private static long getTimeClient() {
        long time = Minecraft.getSystemTime();
        if(Minecraft.getMinecraft().isGamePaused()) {
            timeLag = time - storedTime;
        } else {
            storedTime = time - timeLag;
        }
        return time - timeLag;
    }
    
    private static long getTimeServer() {
        return MinecraftServer.getSystemTimeMillis();
    }

    // In case GameTimer isn't queried frequently, use this to prevent huge (and incorrect) time lag.
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
        getTimeClient();
    }
    
}
