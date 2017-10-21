package cn.lambdalib.util.mc;

import net.minecraft.util.math.BlockPos;

/**
 * Created by Paindar on 17/10/19.
 */
public class EmptyResult extends TraceResult{
    @Override
    public boolean hasPosition() {
        return false;
    }

    @Override
    public BlockPos position(){
        throw new UnsupportedOperationException();
    }
}
