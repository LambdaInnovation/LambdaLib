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
package cn.lambdalib.annoreg.mc;

import java.lang.reflect.Method;

import cn.lambdalib.annoreg.mc.impl.proxy.ServerProxy;
import cn.lambdalib.core.LLModContainer;
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
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ProxyHelper {
	
	static {
		try {
			if (isClient()) {
				proxy = (ServerProxy) Class.forName("cn.lambdalib.annoreg.mc.impl.proxy.ClientProxy").newInstance();
			} else {
				proxy = (ServerProxy) Class.forName("cn.lambdalib.annoreg.mc.impl.proxy.ServerProxy").newInstance();
			}
		} catch (Exception e) {
			LLModContainer.log.fatal("Can not create proxy.");
			throw new RuntimeException(e);
		}
	}
	
	private static ServerProxy proxy;
	
	public static boolean isClient() {
		return FMLCommonHandler.instance().getSide() == Side.CLIENT;
	}
	
	public static void regEntityRender(Class<? extends Entity> clazz, Object obj) {
		proxy.regEntityRender(clazz, obj);
	}
	
	public static void regTileEntityRender(Class<? extends TileEntity> clazz, Object obj) {
		proxy.regTileEntityRender(clazz, obj);
	}
	
	public static void regItemRender(Item item, Object obj) {
		proxy.regItemRender(item, obj);
	}
}
