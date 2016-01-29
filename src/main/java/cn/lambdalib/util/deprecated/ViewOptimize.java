/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

/**
 * This class essentially transforms the origin to the player's hand in thirdPerson or firstPerson.
 * @author WeAthFolD
 */
@Deprecated
public class ViewOptimize {
    
    public interface IAssociatePlayer {
        EntityPlayer getPlayer();
    }
    
    private static final double 
        fpOffsetX = -0.05,
        fpOffsetY = -0.25,
        fpOffsetZ = 0.2;

    private static final double 
        tpOffsetX = 0.15,
        tpOffsetY = -0.8,
        tpOffsetZ = 0.23;
    
    public static void fixFirstPerson() {
        GL11.glTranslated(fpOffsetX, fpOffsetY, fpOffsetZ);
    }
    
    public static void fixThirdPerson() {
        GL11.glTranslated(tpOffsetX, tpOffsetY, tpOffsetZ);
    }
    
    public static void fix(IAssociatePlayer entity) {
        if(isFirstPerson(entity)) {
            fixFirstPerson();
        } else {
            fixThirdPerson();
        }
    }
    
    public static Vec3 getFixVector(IAssociatePlayer entity) {
        if(isFirstPerson(entity)) {
            return Vec3.createVectorHelper(fpOffsetX, fpOffsetY, fpOffsetZ);
        } else {
            return Vec3.createVectorHelper(tpOffsetX, tpOffsetY, tpOffsetZ);
        }
    }
    
    public static boolean isFirstPerson(IAssociatePlayer entity) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer clientPlayer = Minecraft.getMinecraft().thePlayer;
        return mc.gameSettings.thirdPersonView == 0 && clientPlayer == entity.getPlayer();
    }
    
}
