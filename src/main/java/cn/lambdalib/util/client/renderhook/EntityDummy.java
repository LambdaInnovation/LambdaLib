/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client.renderhook;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEntity;
import cn.lambdalib.util.deprecated.ViewOptimize.IAssociatePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
@Registrant
@RegEntity(clientOnly = true)
@RegEntity.HasRender
public class EntityDummy extends Entity implements IAssociatePlayer {
    
    @RegEntity.Render
    public static RenderDummy renderer;
    
    AbstractClientPlayer player;
    final DummyRenderData data;
    
    boolean set;
    float lastRotationYaw, lastRotationYawHead, rotationYawHead;
    float lastRotationPitch;
    
    public EntityDummy(DummyRenderData _data) {
        super(_data.getEntity().worldObj);
        data = _data;
        player = (AbstractClientPlayer) _data.getEntity();
        ignoreFrustumCheck = true;
    }

    @Override
    protected void entityInit() {
    }
    
    @Override
    public void onUpdate() {
        if(!set) {
            set = true;
            lastRotationYaw = player.renderYawOffset;
            lastRotationYawHead = player.rotationYawHead;
            lastRotationPitch = player.rotationPitch;
        } else {
            lastRotationYaw = rotationYaw;
            lastRotationYawHead = rotationYawHead;
            lastRotationPitch = rotationPitch;
        }
        
        posX = player.posX;
        posY = player.posY;
        if(!player.equals(Minecraft.getMinecraft().thePlayer))
            posY += 1.6;
        posZ = player.posZ;
        
        rotationYaw = player.renderYawOffset;
        rotationYawHead = player.rotationYawHead;
        rotationPitch = player.rotationPitch;
    }
    
    @Override
    public EntityPlayer getPlayer() {
        return player;
    }
    
    /**
     * TODO: Support all passes
     */
    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound t) {}

    @Override
    protected void writeEntityToNBT(NBTTagCompound t) {}

}
