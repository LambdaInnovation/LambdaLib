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
package cn.liutils.util.helper;

import java.util.Random;

import cn.liutils.util.generic.DebugUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**
 * A entity that has its position and motion properties. used for all kinds of spacial calculations.
 * @author WeAthFolD
 */
public class Motion3D {

	private static final Random RNG = new Random();
	public static final double OFFSET_SCALE = 0.5D;
	
	public double vx, vy, vz; //Velocity
	public double px, py, pz; //Position

	public Motion3D(double pX, double pY, double pZ, double moX, double moY, double moZ) {
		px = pX;
		py = pY;
		pz = pZ;

		vx = moX;
		vy = moY;
		vz = moZ;
	}

	public Motion3D(Vec3 posVec, double moX, double moY, double moZ) {
		this(posVec.xCoord, posVec.yCoord, posVec.zCoord, moX, moY, moZ);
	}
	
	public Motion3D(Vec3 pos, Vec3 vel) {
		this(pos, vel.xCoord, vel.yCoord, vel.zCoord);
	}
	
	public Motion3D(double x, double y, double z) {
		px = x;
		py = y;
		pz = z;
	}
	
	public Motion3D() {
		this(0, 0, 0, 0, 0, 0);
	}
	
	/**
	 * Create an Motion3D based on its position and moving direction.
	 */
	public Motion3D(Entity ent) {
		this(ent, 0, false);
	}

	/**
	 * @see cn.liutils.api.util.Motion3D(Entity, int, boolean)
	 * @param ent entity
	 * @param dirFlag false:use moving direction; true:use head-looking direction
	 */
	public Motion3D(Entity ent, boolean dirFlag) {
		this(ent, 0, dirFlag);
	}

	/**
	 * Create an Motion3D from an entity, selectible offset.
	 * @param entity
	 * @param offset direction offset
	 * @param dirFlag false:use moving direction; true:use head-looking direction
	 */
	public Motion3D(Entity entity, double offset, boolean dirFlag) {
		init(entity, offset, dirFlag);
	}
	
	@Override
	public Motion3D clone() {
		Motion3D ret = new Motion3D();
		ret.setFrom(this);
		return ret;
	}
	
	public void setFrom(Motion3D ano) {
		px = ano.px;
		py = ano.py;
		pz = ano.pz;
		vx = ano.vx;
		vy = ano.vy;
		vz = ano.vz;
	}
	
	/**
	 * Reset the pos and motion data to the data of entity passed in
	 * @param entity
	 * @param offset
	 * @param dirFlag true: use head facing direction; flase: use motion
	 */
	public void init(Entity entity, double offset, boolean dirFlag) {
		this.px = entity.posX;
		this.py = entity.posY + entity.getEyeHeight();
		this.pz = entity.posZ;

		if (dirFlag) {
			
			float rotationYaw = (float) (
					(entity instanceof EntityLivingBase ? entity.getRotationYawHead() : entity.rotationYaw) 
					+ 2 * (RNG.nextFloat() - 0.5F) * offset);
			float rotationPitch = (float) (entity.rotationPitch + (RNG.nextFloat() - 0.5F) * offset);
			
			fromRotation(rotationYaw, rotationPitch);
			
		} else {
			vx = entity.motionX;
			vy = entity.motionY;
			vz = entity.motionZ;
			setMotionOffset(offset);
		}
		this.normalize();
		
		if(entity instanceof EntityPlayer && entity.worldObj.isRemote) {
			checkClientOffset((EntityPlayer) entity);
		}
	}
	
	@SideOnly(Side.CLIENT)
	private void checkClientOffset(EntityPlayer entity) {
		if(Minecraft.getMinecraft().thePlayer != entity)
			px += 1.6;
	}
	
	public Motion3D fromRotation(float rotationYaw, float rotationPitch) {
		this.vx = -MathHelper.sin(rotationYaw / 180.0F
				* (float) Math.PI)
				* MathHelper.cos(rotationPitch / 180.0F
						* (float) Math.PI);
		this.vz = MathHelper.cos(rotationYaw / 180.0F
				* (float) Math.PI)
				* MathHelper.cos(rotationPitch / 180.0F
						* (float) Math.PI);
		this.vy = -MathHelper.sin((rotationPitch) / 180.0F * (float) Math.PI);
		
		return this;
	}
	
	public float getRotationYaw() {
		return -(float)(Math.atan2(vx, vz) * 180.0D / Math.PI);
	}
	
	public float getRotationYawRadians() {
		return -(float)(Math.atan2(vx, vz));
	}
	
	public float getRotationPitch() {
		return -(float)(Math.atan2(vy, Math.sqrt(vx * vx + vz * vz)) * 180.0D / Math.PI);
	}
	
	public float getRotationPitchRadians() {
		return -(float)(Math.atan2(vy, Math.sqrt(vx * vx + vz * vz)));
	}
	
	public Motion3D setPosition(double x, double y, double z) {
		px = x;
		py = y;
		pz = z;
		return this;
	}
	
	public Motion3D setMotion(double x, double y, double z) {
		vx = x;
		vy = y;
		vz = z;
		return this;
	}
	
	public Motion3D multiplyMotionBy(double d) {
		vx *= d;
		vy *= d;
		vz *= d;
		return this;
	}
	
	/**
	 * Add a 3-dimension randomized offset for the motion vec.
	 * @param par1
	 * @return
	 */
	public Motion3D setMotionOffset(double par1) {
		this.vx += (RNG.nextDouble() - .5) * par1 * OFFSET_SCALE;
		this.vy += (RNG.nextDouble() - .5) * par1 * OFFSET_SCALE;
		this.vz += (RNG.nextDouble() - .5) * par1 * OFFSET_SCALE;
		return this;
	}
	
	/*
	 * Apply the position and rotation data to the entity.
	 * @param mo
	 * @param e
	 */
	public void applyToEntity(Entity e) {
		e.setPosition(this.px, this.py, this.pz);
		e.motionX = vx;
		e.motionY = vy;
		e.motionZ = vz;
		e.prevRotationYaw = e.rotationYaw = getRotationYaw();
        e.prevRotationPitch = e.rotationPitch = getRotationPitch();
	}
	
	/**
	 * Move this motion3D towards motion vec direction. Doesn't copy the class.
	 * @param step
	 * @return this
	 */
	public Motion3D move(double step) {
		px += vx * step;
		py += vy * step;
		pz += vz * step;
		return this;
	}
	
	/**
	 * Normalize the motion vector.
	 * @return this
	 */
	public Motion3D normalize() {
		double z = Math.sqrt(vx * vx + vy * vy + vz * vz);
		vx /= z;
		vy /= z;
		vz /= z;
		return this;
	}
	
	public Vec3 getPosVec() {
		return Vec3.createVectorHelper(px, py, pz);
	}
	
	public Vec3 getMotionVec() {
		return Vec3.createVectorHelper(vx, vy, vz);
	}
	
	@Override
	public String toString() {
		return "[ Motion3D POS" + DebugUtils.formatArray(px, py, pz) + "MOTION" + DebugUtils.formatArray(vx, vy, vz) + " ]";
	}

}
