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
package cn.lambdalib.annoreg.mc.impl.proxy;

import cn.lambdalib.core.LLModContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class ServerProxy {
    
    public void regEntityRender(Class<? extends Entity> clazz, Object obj) {
        LLModContainer.log.fatal("Try to load renderer on server.");
    }
    
    public void regTileEntityRender(Class<? extends TileEntity> clazz, Object obj) {
        LLModContainer.log.fatal("Try to load renderer on server.");
    }
    
    public void regItemRender(Item item, Object obj) {
        LLModContainer.log.fatal("Try to load renderer on server.");
    }
    
    public World getWorld(int dimension) {
        return DimensionManager.getWorld(dimension);
    }

    public Container getPlayerContainer(EntityPlayer player, int windowId) {
        Container ret = player.openContainer;
        if (ret.windowId == windowId) {
            return ret;
        }
        return null;
    }
    
    public EntityPlayer getThePlayer() {
        return null;
    }
    
    public EntityPlayer getPlayerOnServer(String name) {
        return MinecraftServer.getServer().getConfigurationManager().func_152612_a(name);
    }
    
    public Object[] getPlayerList() {
        return MinecraftServer.getServer().getConfigurationManager().playerEntityList.toArray();
    }
    
}