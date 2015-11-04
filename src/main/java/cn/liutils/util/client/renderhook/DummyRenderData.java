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
package cn.liutils.util.client.renderhook;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cn.lambdalib.annoreg.core.Registrant;
import cn.liutils.registry.RegDataPart;
import cn.liutils.util.helper.DataPart;
import cn.liutils.util.helper.PlayerData;
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
@RegDataPart("DummyRender")
public class DummyRenderData extends DataPart {
	
	public static DummyRenderData get(EntityPlayer p) {
		return PlayerData.get(p).getPart(DummyRenderData.class);
	}
	
	private EntityDummy entity;
	List<PlayerRenderHook> renderers = new LinkedList();
	
	@Override
	public void tick() {
		if(entity != null) {
			entity.player = (AbstractClientPlayer) getPlayer();
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
		EntityPlayer player = getPlayer();
		hook.player = player;
		hook.disposed = false;
		
		if(entity == null) {
			player.worldObj.spawnEntityInWorld(
				entity = new EntityDummy(this));
		}
		
		renderers.add(hook);
	}

	@Override
	public NBTTagCompound toNBT() {
		return new NBTTagCompound();
	}

}
