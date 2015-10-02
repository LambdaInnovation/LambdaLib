package cn.liutils.util.raytrace;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import cn.liutils.util.generic.VecUtils;
import cn.liutils.util.helper.Motion3D;
import cn.liutils.util.mc.BlockFilters;
import cn.liutils.util.mc.EntitySelectors;
import cn.liutils.util.mc.IBlockFilter;
import cn.liutils.util.mc.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * A better wrap up for ray trace routines, supporting entity filtering, block filtering, and combined RayTrace of
 * blocks and entities. Also provided functions for fast implementation on entity looking traces.
 * @author WeAthFolD
 */
public class Raytrace {

	/**
	 * Perform a ray trace.
	 * @param world
	 * @param vec1 Start point
	 * @param vec2 End point
	 * @param entitySel The entity filter
	 * @param blockSel The block filter
	 * @return The trace result, might be null
	 */
	public static MovingObjectPosition perform(World world, Vec3 vec1, Vec3 vec2, IEntitySelector entitySel, IBlockFilter blockSel) {
		MovingObjectPosition 
			mop1 = rayTraceEntities(world, vec1, vec2, entitySel),
			mop2 = rayTraceBlocks(world, vec1, vec2, blockSel);

		if(mop1 != null && mop2 != null) {
			double d1 = mop1.hitVec.distanceTo(vec1);
			double d2 = mop2.hitVec.distanceTo(vec1);
			return d1 <= d2 ? mop1 : mop2;
		}
		if(mop1 != null)
			return mop1;
	
		return mop2;
	}
	
	public static MovingObjectPosition perform(World world, Vec3 vec1, Vec3 vec2, IEntitySelector entitySel) {
		return perform(world, vec1, vec2, entitySel, null);
	}
	
	public static MovingObjectPosition perform(World world, Vec3 vec1, Vec3 vec2) {
		return perform(world, vec1, vec2, null, null);
	}
	
	public static Pair<Vec3, MovingObjectPosition> getLookingPos(EntityLivingBase living, double dist) {
		return getLookingPos(living, dist, null, null);
	}
	
	public static Pair<Vec3, MovingObjectPosition> getLookingPos(EntityLivingBase living, double dist, IEntitySelector esel) {
		return getLookingPos(living, dist, esel, null);
	}
	
	public static Pair<Vec3, MovingObjectPosition> getLookingPos(EntityLivingBase living, double dist, IEntitySelector esel, IBlockFilter bsel) {
		MovingObjectPosition pos = traceLiving(living, dist, esel, bsel);
		Vec3 end = null;
		if(pos != null) {
			end = pos.hitVec;
			if(pos.entityHit != null)
				end.yCoord += pos.entityHit.getEyeHeight() * 0.6;
		}
		if(end == null)
			end = new Motion3D(living, true).move(dist).getPosVec();
		
		return Pair.of(end, pos);
	}
	
	public static MovingObjectPosition rayTraceEntities(World world, Vec3 vec1, Vec3 vec2, IEntitySelector selector) {
        Entity entity = null;
        AxisAlignedBB boundingBox = WorldUtils.getBoundingBox(vec1, vec2);
        List list = world.getEntitiesWithinAABBExcludingEntity(null, boundingBox.expand(1.0D, 1.0D, 1.0D), selector);
        double d0 = 0.0D;

        for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = (Entity)list.get(j);

            if(!entity1.canBeCollidedWith() || (selector != null && !selector.isEntityApplicable(entity1)))
            	continue;
            
            float f = 0.3F;
            AxisAlignedBB axisalignedbb = entity1.boundingBox.expand(f, f, f);
            MovingObjectPosition movingobjectposition1 = axisalignedbb.calculateIntercept(vec1, vec2);

            if (movingobjectposition1 != null) {
                double d1 = vec1.distanceTo(movingobjectposition1.hitVec);

                if (d1 < d0 || d0 == 0.0D)
                {
                    entity = entity1;
                    d0 = d1;
                }
            }
        }

        if (entity != null) {
            return new MovingObjectPosition(entity);
        }
        return null;
	}
	
	/**
	 * Mojang code with minor changes to support block filtering.
	 * @param world world
	 * @param vec1 startPoint
	 * @param vec2 endPoint
	 * @param filter BlockFilter
	 * @return MovingObjectPosition
	 */
    @SuppressWarnings("unused")
	public static MovingObjectPosition rayTraceBlocks(World world, Vec3 vec1, Vec3 vec2, IBlockFilter filter) {
        if(Double.isNaN(vec1.xCoord) || Double.isNaN(vec1.yCoord) || Double.isNaN(vec1.zCoord) ||
        		Double.isNaN(vec2.xCoord) || Double.isNaN(vec2.yCoord) || Double.isNaN(vec2.zCoord)) {
        	return null;
        }
        
    	//HACKHACK: copy the vec to prevent modifying the parameter
    	vec1 = VecUtils.copy(vec1);
    	if(filter == null)
    		filter = BlockFilters.filNormal;
        
        int x2 = MathHelper.floor_double(vec2.xCoord);
        int y2 = MathHelper.floor_double(vec2.yCoord);
        int z2 = MathHelper.floor_double(vec2.zCoord);
        int x1 = MathHelper.floor_double(vec1.xCoord);
        int y1 = MathHelper.floor_double(vec1.yCoord);
        int z1 = MathHelper.floor_double(vec1.zCoord);
        Block block = world.getBlock(x1, y1, z1);
        
        int k1 = world.getBlockMetadata(x1, y1, z1);

        if (filter.accepts(world, x1, y1, z1, block))
        {
            MovingObjectPosition movingobjectposition = block.collisionRayTrace(world, x1, y1, z1, vec1, vec2);

            if (movingobjectposition != null)
            {
                return movingobjectposition;
            }
        }

        MovingObjectPosition movingobjectposition2 = null;
        k1 = 200;

        while (k1-- >= 0)
        {
            if (Double.isNaN(vec1.xCoord) || Double.isNaN(vec1.yCoord) || Double.isNaN(vec1.zCoord))
            {
                return null;
            }

            if (x1 == x2 && y1 == y2 && z1 == z2)
            {
                return null;
            }

            boolean flag6 = true;
            boolean flag3 = true;
            boolean flag4 = true;
            double d0 = 999.0D;
            double d1 = 999.0D;
            double d2 = 999.0D;

            if (x2 > x1)
            {
                d0 = (double)x1 + 1.0D;
            }
            else if (x2 < x1)
            {
                d0 = (double)x1 + 0.0D;
            }
            else
            {
                flag6 = false;
            }

            if (y2 > y1)
            {
                d1 = (double)y1 + 1.0D;
            }
            else if (y2 < y1)
            {
                d1 = (double)y1 + 0.0D;
            }
            else
            {
                flag3 = false;
            }

            if (z2 > z1)
            {
                d2 = (double)z1 + 1.0D;
            }
            else if (z2 < z1)
            {
                d2 = (double)z1 + 0.0D;
            }
            else
            {
                flag4 = false;
            }

            double d3 = 999.0D;
            double d4 = 999.0D;
            double d5 = 999.0D;
            double d6 = vec2.xCoord - vec1.xCoord;
            double d7 = vec2.yCoord - vec1.yCoord;
            double d8 = vec2.zCoord - vec1.zCoord;

            if (flag6)
            {
                d3 = (d0 - vec1.xCoord) / d6;
            }

            if (flag3)
            {
                d4 = (d1 - vec1.yCoord) / d7;
            }

            if (flag4)
            {
                d5 = (d2 - vec1.zCoord) / d8;
            }

            boolean flag5 = false;
            byte b0;

            if (d3 < d4 && d3 < d5)
            {
                if (x2 > x1)
                {
                    b0 = 4;
                }
                else
                {
                    b0 = 5;
                }

                vec1.xCoord = d0;
                vec1.yCoord += d7 * d3;
                vec1.zCoord += d8 * d3;
            }
            else if (d4 < d5)
            {
                if (y2 > y1)
                {
                    b0 = 0;
                }
                else
                {
                    b0 = 1;
                }

                vec1.xCoord += d6 * d4;
                vec1.yCoord = d1;
                vec1.zCoord += d8 * d4;
            }
            else
            {
                if (z2 > z1)
                {
                    b0 = 2;
                }
                else
                {
                    b0 = 3;
                }

                vec1.xCoord += d6 * d5;
                vec1.yCoord += d7 * d5;
                vec1.zCoord = d2;
            }

            Vec3 vec32 = Vec3.createVectorHelper(vec1.xCoord, vec1.yCoord, vec1.zCoord);
            x1 = (int)(vec32.xCoord = (double)MathHelper.floor_double(vec1.xCoord));

            if (b0 == 5)
            {
                --x1;
                ++vec32.xCoord;
            }

            y1 = (int)(vec32.yCoord = (double)MathHelper.floor_double(vec1.yCoord));

            if (b0 == 1)
            {
                --y1;
                ++vec32.yCoord;
            }

            z1 = (int)(vec32.zCoord = (double)MathHelper.floor_double(vec1.zCoord));

            if (b0 == 3)
            {
                --z1;
                ++vec32.zCoord;
            }

            Block block1 = world.getBlock(x1, y1, z1);
            int l1 = world.getBlockMetadata(x1, y1, z1);

            if (filter.accepts(world, x1, y1, z1, block1))
            {
                if (true)
                {
                    MovingObjectPosition movingobjectposition1 = block1.collisionRayTrace(world, x1, y1, z1, vec1, vec2);

                    if (movingobjectposition1 != null)
                    {
                        return movingobjectposition1;
                    }
                }
                else
                {
                    movingobjectposition2 = new MovingObjectPosition(x1, y1, z1, b0, vec1, false);
                }
            }
        }

        return null;
    }
	
	public static MovingObjectPosition traceLiving(EntityLivingBase entity, double dist) {
		return traceLiving(entity, dist, null, null);
	}
	
	public static MovingObjectPosition traceLiving(EntityLivingBase entity, double dist, IEntitySelector entitySel) {
		return traceLiving(entity, dist, entitySel, null);
	}
	
	/**
	 * Performs a RayTrace starting from the target entity's eye towards its looking direction.
	 * The trace will automatically ignore the target entity.
	 */
	public static MovingObjectPosition traceLiving(EntityLivingBase entity, double dist, IEntitySelector entitySel, IBlockFilter blockSel) {
		Motion3D mo = new Motion3D(entity, true);
		Vec3 v1 = mo.getPosVec(), v2 = mo.move(dist).getPosVec();
		
		IEntitySelector exclude = EntitySelectors.excludeOf(entity);
		
		return perform(entity.worldObj, v1, v2, entitySel == null ? exclude : EntitySelectors.combine(exclude, entitySel), blockSel);
	}
	

}
