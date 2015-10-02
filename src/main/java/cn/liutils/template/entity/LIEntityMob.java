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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * @author WeAthFolD
 *
 */
public abstract class LIEntityMob extends EntityMob {

	public LIEntityMob(World par1World) {
		super(par1World);
	}

	/**
	 * Change attributes
	 */
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(getMaxHealth2()); // Max Health
		if (getFollowRange() != 0)
			this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(getFollowRange()); // Follow Range
		if (getKnockBackResistance() != 0)
			this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(getKnockBackResistance()); // knockbackResistance
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(getMoveSpeed()); // Move Speed
		this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(getAttackDamage()); // Attack Damage
	}

	abstract protected double getMaxHealth2();

	abstract protected double getFollowRange();

	abstract protected double getMoveSpeed();

	abstract protected double getKnockBackResistance();

	abstract protected double getAttackDamage();

	@SideOnly(Side.CLIENT)
	public abstract ResourceLocation getTexture();

}
