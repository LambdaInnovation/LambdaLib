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
