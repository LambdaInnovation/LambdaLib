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
package cn.liutils.core.event.eventhandler;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * 
 * @author Violet
 *
 */
public abstract class LIHandler<T extends Event> {
	
	private boolean dead = false;
	
	public final boolean isDead() {
		return dead;
	}
	
	public final void setDead() {
		dead = true;
	}
	
	public final void setAlive() {
		dead = false;
	}

	public final void trigger(T event) {
		if (!onEvent(event))
			throw new RuntimeException("Unexpected event(" + event.getClass().getName() + ") for " + this.getClass().getName());
	}
	
	/**
	 * Return false if some error occured.
	 * SUGGESTION: return false if this handler should be set dead?
	 */
	protected abstract boolean onEvent(T event);
}
