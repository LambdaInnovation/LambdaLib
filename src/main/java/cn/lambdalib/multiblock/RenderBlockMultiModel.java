/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.multiblock;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.deprecated.ITileEntityModel;

/**
 * Simple model renderer template.
 * 
 * @author WeathFolD
 */
public class RenderBlockMultiModel extends RenderBlockMulti {

    protected ITileEntityModel mdl;
    protected ResourceLocation tex;
    protected double scale = 1.0;
    protected double rotateY = 0.0;

    public RenderBlockMultiModel(ITileEntityModel _mdl, ResourceLocation _tex) {
        mdl = _mdl;
        tex = _tex;
    }

    public RenderBlockMultiModel() {
    }

    public RenderBlockMultiModel setScale(double f) {
        scale = f;
        return this;
    }

    @Override
    public void drawAtOrigin(TileEntity te) {
        GL11.glColor4d(1, 1, 1, 1);
        if (tex != null) {
            RenderUtils.loadTexture(tex);
        }
        GL11.glRotated(rotateY, 0, 1, 0);
        GL11.glScaled(scale, scale, scale);
        mdl.render(te, 0, 0);
    }

}
