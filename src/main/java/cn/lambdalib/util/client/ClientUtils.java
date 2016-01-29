/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client;

import cn.lambdalib.util.client.auxgui.AuxGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * Client-Side judgement helper and other stuffs.
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
public class ClientUtils {
    
    /**
     * Judge if the player is playing the client game and isn't opening any GUI.
     * @return
     */
    public static boolean isPlayerInGame() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        return player != null && Minecraft.getMinecraft().currentScreen == null && !AuxGuiHandler.hasForegroundGui();
    }

    public static boolean isInWorld() {
        return Minecraft.getMinecraft().thePlayer != null;
    }
    
    public static boolean isPlayerPlaying() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.thePlayer != null && !mc.isGamePaused();
    }
    
    /**
     * Quick alias for playing static sound
     * @param src
     * @param pitch
     */
    public static void playSound(ResourceLocation src, float pitch) {
        Minecraft.getMinecraft().getSoundHandler().playSound(
            PositionedSoundRecord.func_147674_a(src, pitch));
    }

}
