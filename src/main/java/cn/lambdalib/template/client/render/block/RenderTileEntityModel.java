/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.template.client.render.block;

import java.util.Random;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.deprecated.ITileEntityModel;

// TODO Enhance the abstraction
public class RenderTileEntityModel extends TileEntitySpecialRenderer {

    private ITileEntityModel model;
    private ResourceLocation texture;
    protected static Random RNG = new Random();
    protected double yOffset = 0F;
    protected boolean reverse = false;
    protected double scale = 1F;
    
    public RenderTileEntityModel(ITileEntityModel mo, ResourceLocation tex) {
        model = mo;
        texture = tex;
    }
    
    public RenderTileEntityModel setReverse(boolean b) {
        reverse = b;
        return this;
    }
    
    public RenderTileEntityModel setYOffset(double f) {
        yOffset = f;
        return this;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1,
            double d2, float f) {
        GL11.glPushMatrix(); {
            GL11.glTranslated(d0 + .5, d1 + yOffset, d2 + .5);
            if(reverse) {
                GL11.glScaled(-scale, -scale, scale);
            } else {
                GL11.glScaled(scale, scale, scale);
            }
            RenderUtils.loadTexture(texture);
            model.render(tileentity, 0F, 0F);
        } GL11.glPopMatrix();
    }

}
