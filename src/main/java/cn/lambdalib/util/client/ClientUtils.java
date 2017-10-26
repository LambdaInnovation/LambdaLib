/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client;

import cn.lambdalib.util.client.auxgui.AuxGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        EntityPlayer player = Minecraft.getMinecraft().player;
        return player != null && Minecraft.getMinecraft().currentScreen == null && !AuxGuiHandler.hasForegroundGui();
    }

    public static boolean isInWorld() {
        return Minecraft.getMinecraft().player != null;
    }
    
    public static boolean isPlayerPlaying() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc.player != null && !mc.isGamePaused();
    }
    
    /**
     * Quick alias for playing static sound
     * @param src
     * @param pitch
     */
    public static void playSound(ResourceLocation src, SoundCategory cat, float pitch) {
        Minecraft.getMinecraft().getSoundHandler().playSound(
            new PositionedSoundRecord(src, cat, 0.25F,pitch, false, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F));
    }
    /*
        public static PositionedSoundRecord func_147674_a(ResourceLocation p_147674_0_, float p_147674_1_)
    {
        return new PositionedSoundRecord(p_147674_0_, 0.25F, p_147674_1_, false, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
    }
     */

}
