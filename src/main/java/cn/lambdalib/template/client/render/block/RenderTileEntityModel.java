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
