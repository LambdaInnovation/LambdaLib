/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

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
