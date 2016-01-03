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
