/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.template.client.render.block;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegBlockRenderer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Renders nothing at all.
 * @author WeathFolD
 */
@SideOnly(Side.CLIENT)
@Registrant
@RegBlockRenderer
public class RenderEmptyBlock implements ISimpleBlockRenderingHandler {
    
    public static int id = RenderingRegistry.getNextAvailableRenderId();

    public RenderEmptyBlock() {}

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID,
            RenderBlocks renderer) {
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
            Block block, int modelId, RenderBlocks renderer) {
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelID) {
        return false;
    }

    @Override
    public int getRenderId() {
        return id;
    }

}
