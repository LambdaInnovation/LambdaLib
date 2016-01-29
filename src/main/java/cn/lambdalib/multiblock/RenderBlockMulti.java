/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.multiblock;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

/**
 * The BlockMulti base render, which focuses on placement judging. Concrete
 * render ways belongs to its subclasses.
 * 
 * @author WeathFolD
 */
public abstract class RenderBlockMulti extends TileEntitySpecialRenderer {

    public RenderBlockMulti() {
    }

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f) {
        if (!(te.getBlockType() instanceof BlockMulti))
            return;

        BlockMulti bm = (BlockMulti) te.getBlockType();
        InfoBlockMulti inf = ((IMultiTile) te).getBlockInfo();

        if (inf == null || !inf.isLoaded() || inf.subID != 0)
            return;
        GL11.glPushMatrix();
        double[] off = bm.getPivotOffset(inf);
        double[] off2 = bm.rotCenters[inf.dir.ordinal()];
        GL11.glTranslated(x + off[0] + off2[0], y + 0 + off2[1], z + off[1] + off2[2]);
        // GL11.glTranslated(x, y, z);
        GL11.glRotated(bm.getRotation(inf), 0, 1, 0);
        drawAtOrigin(te);
        GL11.glPopMatrix();
    }

    public abstract void drawAtOrigin(TileEntity te);

}
