/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.mc;

import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import cn.lambdalib.util.generic.VecUtils;
import cn.lambdalib.util.helper.Motion3D;
import net.minecraft.block.Block;
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
     * @param world The world to perform on
     * @param vec1 Start point
     * @param vec2 End point
     * @param entityPred The entity predicate
     * @param blockSel The block predicate
     * @return The trace result, might be null
     */
    public static MovingObjectPosition perform(World world, Vec3 vec1, Vec3 vec2, Predicate<Entity> entityPred, IBlockSelector blockSel) {
        MovingObjectPosition 
            mop1 = rayTraceEntities(world, vec1, vec2, entityPred),
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
    
    public static MovingObjectPosition perform(World world, Vec3 vec1, Vec3 vec2, Predicate<Entity> entityPred) {
        return perform(world, vec1, vec2, entityPred, null);
    }
    
    public static MovingObjectPosition perform(World world, Vec3 vec1, Vec3 vec2) {
        return perform(world, vec1, vec2, null, null);
    }
    
    public static Pair<Vec3, MovingObjectPosition> getLookingPos(EntityLivingBase living, double dist) {
        return getLookingPos(living, dist, null, null);
    }
    
    public static Pair<Vec3, MovingObjectPosition> getLookingPos(EntityLivingBase living, double dist,
                                                                 Predicate<Entity> pred) {
        return getLookingPos(living, dist, pred, null);
    }
    
    public static Pair<Vec3, MovingObjectPosition> getLookingPos(Entity living, double dist,
                                                                 Predicate<Entity> esel, IBlockSelector bsel) {
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
    
    public static MovingObjectPosition rayTraceEntities(World world, Vec3 vec1, Vec3 vec2, Predicate<Entity> selector) {
        Entity entity = null;
        AxisAlignedBB boundingBox = WorldUtils.getBoundingBox(vec1, vec2);
        List list = world.getEntitiesWithinAABBExcludingEntity(null, boundingBox.expand(1.0D, 1.0D, 1.0D),
                EntitySelectors.toEntitySelector(selector));
        double d0 = 0.0D;

        for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = (Entity)list.get(j);

            if(!entity1.canBeCollidedWith() || (selector != null && !selector.test(entity1)))
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
     * @param start startPoint
     * @param end endPoint
     * @param filter BlockFilter
     * @return MovingObjectPosition
     */
    public static MovingObjectPosition rayTraceBlocks(World world, Vec3 start, Vec3 end, IBlockSelector filter) {
        if(filter == null)
            filter = BlockSelectors.filNormal;

        Vec3 current = VecUtils.copy(start);
        
        final int x2 = MathHelper.floor_double(end.xCoord);
        final int y2 = MathHelper.floor_double(end.yCoord);
        final int z2 = MathHelper.floor_double(end.zCoord);

        int x1 = MathHelper.floor_double(current.xCoord);
        int y1 = MathHelper.floor_double(current.yCoord);
        int z1 = MathHelper.floor_double(current.zCoord);
        {
            Block block = world.getBlock(x1, y1, z1);
            if (filter.accepts(world, x1, y1, z1, block)) {
                MovingObjectPosition result = block.collisionRayTrace(world, x1, y1, z1, current, end);
                if (result != null) {
                    return result;
                }
            }
        }

        for (int i = 0; i < 200; ++i) {
            if (x1 == x2 && y1 == y2 && z1 == z2) {
                return null;
            }

            boolean moveX = true;
            boolean moveY = true;
            boolean moveZ = true;
            double nextX = 999.0D;
            double nextY = 999.0D;
            double nextZ = 999.0D;

            if (x2 > x1) {
                nextX = x1 + 1.0D;
            } else if (x2 < x1) {
                nextX = x1 + 0.0D;
            } else {
                moveX = false;
            }

            if (y2 > y1) {
                nextY = y1 + 1.0D;
            } else if (y2 < y1) {
                nextY = y1 + 0.0D;
            } else {
                moveY = false;
            }

            if (z2 > z1) {
                nextZ = z1 + 1.0D;
            } else if (z2 < z1) {
                nextZ = z1 + 0.0D;
            } else {
                moveZ = false;
            }

            double xFactor = 999.0D;
            double yFactor = 999.0D;
            double zFactor = 999.0D;
            double dx = end.xCoord - current.xCoord;
            double dy = end.yCoord - current.yCoord;
            double dz = end.zCoord - current.zCoord;

            if (moveX) {
                xFactor = (nextX - current.xCoord) / dx;
            }
            if (moveY) {
                yFactor = (nextY - current.yCoord) / dy;
            }
            if (moveZ) {
                zFactor = (nextZ - current.zCoord) / dz;
            }
            byte side;

            if (xFactor < yFactor && xFactor < zFactor) {
                if (x2 > x1) {
                    side = 4;
                } else {
                    side = 5;
                }

                current.xCoord = nextX;
                current.yCoord += dy * xFactor;
                current.zCoord += dz * xFactor;
            } else if (yFactor < zFactor) {
                if (y2 > y1) {
                    side = 0;
                } else {
                    side = 1;
                }

                current.xCoord += dx * yFactor;
                current.yCoord = nextY;
                current.zCoord += dz * yFactor;
            } else {
                if (z2 > z1) {
                    side = 2;
                } else {
                    side = 3;
                }

                current.xCoord += dx * zFactor;
                current.yCoord += dy * zFactor;
                current.zCoord = nextZ;
            }

            x1 = MathHelper.floor_double(current.xCoord);
            if (side == 5) {
                --x1;
            }

            y1 = MathHelper.floor_double(current.yCoord);
            if (side == 1) {
                --y1;
            }

            z1 = MathHelper.floor_double(current.zCoord);
            if (side == 3) {
                --z1;
            }

            Block block = world.getBlock(x1, y1, z1);
            if (filter.accepts(world, x1, y1, z1, block)) {
                MovingObjectPosition result = block.collisionRayTrace(world, x1, y1, z1, current, end);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
    
    public static MovingObjectPosition traceLiving(Entity entity, double dist) {
        return traceLiving(entity, dist, null, null);
    }
    
    public static MovingObjectPosition traceLiving(Entity entity, double dist, Predicate<Entity> pred) {
        return traceLiving(entity, dist, pred, null);
    }
    
    /**
     * Performs a RayTrace starting from the target entity's eye towards its looking direction.
     * The trace will automatically ignore the target entity.
     */
    public static MovingObjectPosition traceLiving(Entity entity, double dist, Predicate<Entity> pred, IBlockSelector blockSel) {
        Motion3D mo = new Motion3D(entity, true);
        Vec3 v1 = mo.getPosVec(), v2 = mo.move(dist).getPosVec();
        
        Predicate<Entity> exclude = EntitySelectors.exclude(entity);
        
        return perform(entity.worldObj, v1, v2, pred == null ? exclude : exclude.or(pred), blockSel);
    }
    

}
