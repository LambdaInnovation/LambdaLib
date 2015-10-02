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

import java.util.LinkedList;



import cn.liutils.core.entity.SamplePoint;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Entity trail entity.
 * @author WeAthFolD
 */
public class EntityTrailFX extends Entity {

	private LinkedList<SamplePoint> samples = new LinkedList();
	private Entity linkedEntity;
	private ResourceLocation texNormal, texEnd;
	private boolean renderEnd, hasLight;
	private int decayTime, sampleFreq;
	private double width;

	public EntityTrailFX(World par1World, Entity par2LinkedEntity) {
		super(par1World);
		this.linkedEntity = par2LinkedEntity;
		texNormal = texEnd = null;
		renderEnd = false;
		decayTime = 20;
		sampleFreq = 2;
		this.posX = linkedEntity.posX;
		this.posY = linkedEntity.posY;
		this.posZ = linkedEntity.posZ;
		this.width = 0.5;
		this.ignoreFrustumCheck = true;
	}
	
	public EntityTrailFX setHasLight(boolean b) {
		this.hasLight = b;
		return this;
	}
	
	public boolean getHasLight() {
		return hasLight;
	}

	public EntityTrailFX setTrailWidth(double w) {
		width = w;
		return this;
	}

	public EntityTrailFX setTextures(ResourceLocation n, ResourceLocation e) {
		texNormal = n;
		texEnd = e;
		return this;
	}

	public EntityTrailFX setDoesRenderEnd(boolean b) {
		renderEnd = b;
		return this;
	}

	public EntityTrailFX setDecayTime(int time) {
		decayTime = time;
		return this;
	}

	public EntityTrailFX setSampleFreq(int freq) {
		sampleFreq = freq;
		return this;
	}
	
	@Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

	@Override
	protected void entityInit() {
	}

	@Override
	public void onUpdate() {
		if (posX == 0 && posY == 0 && posZ == 0) {
			posX = linkedEntity.posX;
			posY = linkedEntity.posY;
			posZ = linkedEntity.posZ;
		}

		if (this.ticksExisted % sampleFreq == 0) {
			if (ticksExisted > 2 * decayTime)
				samples.removeFirst();
			if (linkedEntity.isDead) {
				if (samples.size() <= 0)
					this.setDead();
				return;
			}
			samples.offer(new SamplePoint(linkedEntity.posX - posX,
					linkedEntity.posY - posY, linkedEntity.posZ - posZ,
					ticksExisted));
		}
	}

	public ResourceLocation getTexNormal() {
		return texNormal;
	}

	public ResourceLocation getTexEnd() {
		return texEnd;
	}

	public double getTrailWidth() {
		return width;
	}

	public int getDecayTime() {
		return this.decayTime;
	}

	public Boolean doesRenderEnd() {
		return renderEnd;
	}

	public LinkedList<SamplePoint> getSamplePoints() {
		return (LinkedList<SamplePoint>) samples.clone();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
	}

}
