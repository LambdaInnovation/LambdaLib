/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client.renderhook;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.util.datapart.DataPart;
import cn.lambdalib.util.datapart.EntityData;
import cn.lambdalib.util.datapart.RegDataPart;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
@Registrant
@RegDataPart(EntityPlayer.class)
public class DummyRenderData extends DataPart<EntityPlayer> {

    public DummyRenderData() {
        setTick(true);
    }
    
    public static DummyRenderData get(EntityPlayer p) {
        return EntityData.get(p).getPart(DummyRenderData.class);
    }
    
    private EntityDummy entity;
    List<PlayerRenderHook> renderers = new LinkedList();
    
    @Override
    public void tick() {
        if(entity != null) {
            entity.player = (AbstractClientPlayer) getEntity();
        }
        
        Iterator<PlayerRenderHook> iter = renderers.iterator();
        while(iter.hasNext()) {
            PlayerRenderHook val = iter.next();
            if(val.disposed)
                iter.remove();
        }
        
        // Destroy the entity when no more needed, saving resources
        if(entity != null && renderers.size() == 0) {
            entity.setDead();
            entity = null;
        }
    }
    
    @Override
    public void fromNBT(NBTTagCompound tag) {
        // N/A
    }
    
    public void addRenderHook(PlayerRenderHook hook) {
        EntityPlayer player = getEntity();
        hook.player = player;
        hook.disposed = false;
        
        if(entity == null) {
            player.worldObj.spawnEntityInWorld(
                entity = new EntityDummy(this));
        }
        
        renderers.add(hook);
    }

    @Override
    public void toNBT(NBTTagCompound tag) {
    }

}
