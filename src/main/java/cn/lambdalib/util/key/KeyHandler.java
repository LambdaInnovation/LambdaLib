/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.key;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class KeyHandler {

    public void onKeyDown() {}
    
    public void onKeyUp() {}
    
    /**
     * This happens when the KeyBinding is a non-global one, 
     * and player opens any GUI or jumps out of the game.
     */
    public void onKeyAbort() {}
    
    public void onKeyTick() {}
    
    @SideOnly(Side.CLIENT)
    protected Minecraft getMC() {
        return Minecraft.getMinecraft();
    }
    
    @SideOnly(Side.CLIENT)
    protected EntityPlayer getPlayer() {
        return getMC().thePlayer;
    }
    
}
