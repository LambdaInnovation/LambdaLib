package cn.lambdalib.util.mc;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

/**
 * Created by Paindar on 17/10/19.
 */
public class EntityResult extends TraceResult
{
    private Entity target;
    public EntityResult(Entity entity){

    }
    @Override
    public boolean hasPosition()
    {
        return true;
    }

    @Override
    public BlockPos position()
    {
        return target.getPosition();
    }
}
