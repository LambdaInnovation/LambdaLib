/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.particle;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author WeAthFolD
 */
public interface ISpriteEntity {

    /**
     * Called each rendering frame before rendering to update the sprite's state
     */
    @SideOnly(Side.CLIENT)
    void updateSprite(Sprite s);

    boolean needViewOptimize();

}
