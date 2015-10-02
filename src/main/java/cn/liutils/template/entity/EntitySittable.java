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
package cn.liutils.template.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cn.annoreg.core.Registrant;
import cn.annoreg.mc.RegEntity;

/**
 * TileSittable的支持实体~
 * 
 * @author WeAthFolD
 */
@Registrant
@RegEntity
public class EntitySittable extends Entity {
	/**
	 * Marking interface.
	 */
	public static interface ISittable {}
	
	public EntityPlayer mountedPlayer;
	Vec3 startPt;
	int bx, by, bz;

	public EntitySittable(World wrld, float x, float y, float z, int bx,
			int by, int bz) {
		super(wrld);
		setPosition(x, y, z);
		setSize(0.01F, 0.01F);
		startPt = Vec3.createVectorHelper(x, y, z);
		this.bx = bx;
		this.by = by;
		this.bz = bz;
	}

	public EntitySittable(World wrld) {
		super(wrld);
		setSize(0.01F, 0.01F);
	}

	public void mount(EntityPlayer player) {
		player.mountEntity(this);
		mountedPlayer = player;
	}

	public void disMount() {
		if (mountedPlayer != null) {
			mountedPlayer.mountEntity((Entity) null);
			mountedPlayer = null;
		}
	}

	public boolean isMounted() {
		return mountedPlayer != null;
	}

	@Override
	public void onUpdate() {
		if(mountedPlayer != null && mountedPlayer.isDead) {
			mountedPlayer = null;
		}
		
		if (!worldObj.isRemote) {
			TileEntity te = worldObj.getTileEntity(bx, by, bz);
			if (te == null || !(te instanceof ISittable)) {
				setDead();
				return;
			}
			if (startPt != null) {
				posX = startPt.xCoord;
				posY = startPt.yCoord;
				posZ = startPt.zCoord;
			} else {
				setDead();
			}
		}
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		setDead();
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
	}
}