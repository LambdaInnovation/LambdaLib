/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import net.minecraft.item.ItemStack;

/**
 * Interface for model applied on itemRenderer.
 * @author WeathFolD
 */
@Deprecated
public interface IItemModel {

    public void render(ItemStack is, float scale, float f);
    public void setRotationAngles(ItemStack is, double posX, double posY, double posZ, float f);
    
}

