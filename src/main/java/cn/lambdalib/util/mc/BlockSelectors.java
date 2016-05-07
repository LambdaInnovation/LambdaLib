/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.mc;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockSelectors {
    
    public static final IBlockSelector

    filNothing = (world, x, y, z, block) -> block != Blocks.air,

    filNormal = (world, x, y, z, block) -> {
        Block b = world.getBlock(x, y, z);
        return b.getCollisionBoundingBoxFromPool(world, x, y, z) != null &&
                b.canCollideCheck(world.getBlockMetadata(x, y , z), false);
    },
    
    filEverything = (world, x, y, z, block) -> false,

    filReplacable = (world, x, y, z, block) -> !block.isReplaceable(world, x, y, z);

}
