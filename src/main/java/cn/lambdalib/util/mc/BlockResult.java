package cn.lambdalib.util.mc;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Paindar on 17/10/19.
 */
public class BlockResult extends TraceResult{

    private BlockPos pos;
    private EnumFacing side;
    public BlockResult(BlockPos pos, EnumFacing side){
        this.pos = pos;
        this.side = side;
    }
    public Block getBlock(World world){
        return world.getBlockState(pos).getBlock();
    }
    public TileEntity getTileEntity(World world){
        return world.getTileEntity(pos);
    }

    @Override
    public boolean hasPosition(){
        return true;
    }

    @Override
    public  BlockPos position(){
        return pos.add( .5, .5, .5);
    }
}
