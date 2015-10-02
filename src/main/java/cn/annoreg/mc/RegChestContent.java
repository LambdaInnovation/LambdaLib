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
package cn.annoreg.mc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add your own ChestContent to ChestHooks.<br />
 * The parameter of this is the type of dungeon you want to add. Put an int[] as the parameter.<br />
 * List of number:<br />
 * 0 - Dungeon Chest<br />
 * 1 - Village Blacksmith<br />
 * 2 - Deserty Pyramid Chest<br />
 * 3 - Jungle Pyramid Chest<br />
 * 4 - Mineshaft Corridor<br />
 * 5 - Jungle Pyramid Dispenser<br />
 * 6 - Stronghold Corridor<br />
 * 7 - Stronghold Library<br />
 * 8 - Stronghold Crossing<br />
 * 9 - Bonus Chest
 * @author KSkun
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegChestContent {
	int[] value();
}
