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
package cn.lambdalib.util.helper;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * World+xyz, representing a single block position.
 * @author WeAthFolD
 */
public class BlockPos {

    public final World world;
    
    public final int x, y, z;
    
    public BlockPos(World _world, int _x, int _y, int _z) {
        world = _world;
        x = _x;
        y = _y;
        z = _z;
    }
    
    public Block getBlock() {
        return world.getBlock(x, y, z);
    }
    
    public TileEntity getTile() {
        return world.getTileEntity(x, y, z);
    }
    
    @Override
    public String toString() {
        return "BlockPos[" + world.provider.dimensionId + " (" + x + ", " + y + ", " + z + ")]";
    }
    
    @Override
    public int hashCode() {
        return x ^ y ^ z + world.provider.dimensionId;
    }
    
    @Override
    public boolean equals(Object another) {
        if(!(another instanceof BlockPos))
            return false;
        BlockPos b = (BlockPos) another;
        return b.world.equals(world) && b.x == x && b.y == y && b.z == z;
    }
    
}
