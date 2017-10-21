package cn.lambdalib.util.mc;


import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;


public abstract class TraceResult
{
    public Object apply(RayTraceResult mop)
    {
        if (mop == null || mop.typeOfHit == RayTraceResult.Type.MISS) {
            return new EmptyResult();
        } else if (mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            return new BlockResult(mop.getBlockPos(), mop.sideHit);
        }
        else { // Entity
            return new EntityResult(mop.entityHit);
        }
    }

    public abstract boolean hasPosition();

    public abstract BlockPos position();

}
