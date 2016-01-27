/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import org.lwjgl.opengl.GL11;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.IModelCustom;

/**
 * An simple adaptor from IModelCustom to IItemModel. <br>
 * Deprecated. use {@link cn.lambdalib.vis.model.renderer.ItemModelRenderer} instead.
 * @author WeAthFolD
 */
@Deprecated
public class ItemModelCustom implements IItemModel {

    private IModelCustom theModel;
    
    public ItemModelCustom(IModelCustom model) {
        theModel = model;
    }

    @Override
    public void render(ItemStack is, float scale, float f) {
        GL11.glScalef(scale, scale, scale);
        theModel.renderAll();
    }

    @Override
    public void setRotationAngles(ItemStack is, double posX, double posY,
            double posZ, float f) {
    }

}
