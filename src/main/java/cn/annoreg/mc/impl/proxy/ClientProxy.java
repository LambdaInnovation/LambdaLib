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
package cn.annoreg.mc.impl.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends ServerProxy {
	@Override
	public void regEntityRender(Class<? extends Entity> clazz, Object obj) {
		RenderingRegistry.registerEntityRenderingHandler(clazz, (Render) obj);
	}

	@Override
	public void regTileEntityRender(Class<? extends TileEntity> clazz, Object obj) {
		ClientRegistry.bindTileEntitySpecialRenderer(clazz, (TileEntitySpecialRenderer) obj);
	}
	
	@Override
	public void regItemRender(Item item, Object obj) {
		MinecraftForgeClient.registerItemRenderer(item, (IItemRenderer) obj);
	}
	
	@Override
	public World getWorld(int dimension) {
		World theWorld = Minecraft.getMinecraft().theWorld;
		if (theWorld != null && theWorld.provider.dimensionId == dimension) {
			return theWorld;
		} else {
			return null;
		}
	}
	
	@Override
	public Container getPlayerContainer(EntityPlayer player, int windowId) {
		Container ret = Minecraft.getMinecraft().thePlayer.openContainer;
		if (ret.windowId == windowId) {
			return ret;
		} else {
			return null;
		}
	}

    @Override
	public EntityPlayer getThePlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

    @Override
    public EntityPlayer getPlayerOnServer(String name) {
        return null;
    }
    
    @Override
    public Object[] getPlayerList() {
        return new Object[] {};
    }
}