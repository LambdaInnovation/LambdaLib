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
package cn.lambdalib.annoreg.mc.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public abstract class GuiHandlerBase {
	
	private Object mod;
	private int guiId;
	
	private boolean isClientGuiOpening;
	
	private IGuiHandler guiHandler = new IGuiHandler() {
		@Override
		public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
			return getServerContainer(player, world, x, y, z);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
			if (isClientGuiOpening) {
				isClientGuiOpening = false;
				return getClientGui();
			} else {
				return getClientContainer(player, world, x, y, z);
			}
		}
	};
	
	/*
	 * API for registry part.
	 */
	
	void register(Object mod, int guiId) {
		this.mod = mod;
		this.guiId = guiId;
	}
	
	IGuiHandler getHandler() {
		return guiHandler;
	}
	
	/*
	 * API for external call.
	 */
	
	/**
	 * Open a gui container. Should be called on SERVER ONLY.
	 * Side check is enforced.
	 * @param player
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	public final void openGuiContainer(EntityPlayer player, World world, int x, int y, int z) {
		if(!world.isRemote)
			player.openGui(mod, guiId, world, x, y, z);
	}
	
	/**
	 * Called on client side to just open a GUI on client.
	 */
	@SideOnly(Side.CLIENT)
	public final void openClientGui() {
		clientGui();
	}
	
	/**
	 * Only open a GUI on client.
	 * Can be overridden if you wants other behaviors not only just opening a GUI.
	 */
	@SideOnly(Side.CLIENT)
	protected void clientGui() {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		isClientGuiOpening = true;
		player.openGui(mod, guiId, player.worldObj, 0, 0, 0);
	}
	
	/*
	 * GUI/Container generation 
	 */
	
	@SideOnly(Side.CLIENT)
	protected GuiScreen getClientGui() {
		throw new UnsupportedOperationException();
	}

	@SideOnly(Side.CLIENT)
	protected Object getClientContainer(EntityPlayer player, World world, int x, int y, int z) {
		throw new UnsupportedOperationException();
	}
	
	protected Object getServerContainer(EntityPlayer player, World world, int x, int y, int z) {
		throw new UnsupportedOperationException();
	}
}
