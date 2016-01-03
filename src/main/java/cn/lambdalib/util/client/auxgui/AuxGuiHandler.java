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
package cn.lambdalib.util.client.auxgui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEventHandler;
import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.helper.GameTimer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;

/**
 * @author WeathFolD
 *
 */
@SideOnly(Side.CLIENT)
@Registrant
public class AuxGuiHandler {

    @RegEventHandler
    public static AuxGuiHandler instance = new AuxGuiHandler();
    
    private AuxGuiHandler() {}
    
    private static boolean iterating;
    private static List<AuxGui> auxGuiList = new LinkedList<AuxGui>();
    private static List<AuxGui> toAddList = new ArrayList();
    
    public static void register(AuxGui gui) {
        if(!iterating)
            doAdd(gui);
        else
            toAddList.add(gui);
    }
    
    private static void doAdd(AuxGui gui) {
        auxGuiList.add(gui);
        MinecraftForge.EVENT_BUS.post(new OpenAuxGuiEvent(gui));
        gui.onAdded();
    }
    
    private static void startIterating() {
        iterating = true;
    }
    
    private static void endIterating() {
        iterating = false;
    }
    
    @SubscribeEvent    
    public void drawHudEvent(RenderGameOverlayEvent event) {
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtils.pushTextureState();
        
        if(event.type == ElementType.EXPERIENCE) {
            Iterator<AuxGui> iter = auxGuiList.iterator();
            startIterating();
            while(iter.hasNext()) {
                AuxGui gui = iter.next();
                if(!gui.isDisposed()) {
                    if(!gui.lastFrameActive)
                        gui.lastActivateTime = GameTimer.getTime();
                    gui.draw(event.resolution);
                    gui.lastFrameActive = true;
                }
            }
            endIterating();
        }
        
        RenderUtils.popTextureState();
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDepthMask(true);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }
    
    @SubscribeEvent
    public void clientTick(ClientTickEvent event) {
        if(!Minecraft.getMinecraft().isGamePaused()) {
            for(AuxGui gui : toAddList)
                doAdd(gui);
            toAddList.clear();
            
            Iterator<AuxGui> iter = auxGuiList.iterator();
            startIterating();
            while(iter.hasNext()) {
                AuxGui gui = iter.next();
                
                if(gui.isDisposed()) {
                    gui.onDisposed();
                    gui.lastFrameActive = false;
                    iter.remove();
                } else if(gui.requireTicking) {
                    if(!gui.lastFrameActive)
                        gui.lastActivateTime = GameTimer.getTime();
                    gui.tick();
                    gui.lastFrameActive = true;
                }
            }
            endIterating();
        }
    }
    
    @SubscribeEvent
    public void disconnected(ClientDisconnectionFromServerEvent event) {
        startIterating();
        Iterator<AuxGui> iter = auxGuiList.iterator();
        while(iter.hasNext()) {
            AuxGui gui = iter.next();
            if(!gui.isConsistent()) {
                gui.onDisposed();
                iter.remove();
            }
        }
        endIterating();
    }
    
    public static boolean hasForegroundGui() {
        boolean result = false;
        
        startIterating();
        for(AuxGui ag : auxGuiList) {
            if(!ag.isDisposed() && ag.isForeground()) {
                result = true;
                break;
            }
        }
        endIterating();
        return result;
    }
    
}
