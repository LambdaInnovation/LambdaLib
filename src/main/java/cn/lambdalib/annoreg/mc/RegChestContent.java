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
 * Add your own ChestContent to LootPool.<br />
 *
 * The parameter of this is the type of dungeon you want to add. Put an int[] as the parameter.<br />
 * size is how much item you want to generate.
 * prob is how much probability item would be generate.
 * List of number:<br />
 * 0 - spawn_bonus_chest
 * 1 - end_city_treasure
 * 2 - imple_dungeon
 * 3 - village_blacksmith
 * 4 - abandoned_mineshaft
 * 5 - nether_bridge
 * 6 - stronghold_library
 * 7 - stronghold_crossing
 * 8 - stronghold_corridor
 * 9 - desert_pyramid
 *10 - jungle_temple
 *11 - jungle_temple_dispenser
 *12 - igloo_chest
 *13 - woodland_mansion
 * @author Paindar
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RegChestContent {
    int size()default 1;
    int prob()default 50;
    int[] value();
}
