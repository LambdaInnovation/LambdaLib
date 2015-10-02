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
package cn.liutils.core.entity;

import net.minecraft.entity.Entity;

/**
 * A utility class to enable one entity to link to another.
 * @author WeAthFolD
 * @see  cn.lambdacraft.mob.util.MobHelper#spawnCreature
 * @param <T> Entity type
 */
public interface IEntityLink<T extends Entity> {

	/**
	 * Get the linked entity.
	 * @return
	 */
	public T getLinkedEntity();

	/**
	 * Set the linked entity.
	 * @param entity
	 */
	public void setLinkedEntity(T entity);

}
