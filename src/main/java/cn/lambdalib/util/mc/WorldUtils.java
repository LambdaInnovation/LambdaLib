/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.mc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Predicate;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * Utils about block/entity lookup and interaction.
 * @author WeAthFolD
 */
public class WorldUtils {

    /**
     * Judge whether a world is valid (e.g. currently in use), and false if world is null.
     */
    public static boolean isWorldValid(World world) {
        if(world == null) {
            return false;
        }
        if(SideHelper.isClient()) {
            return worldValidC(world);
        } else {
            WorldServer[] wss = MinecraftServer.getServer().worldServers;
            for(World w : wss) {
                if(w == world) {
                    return true;
                }
            }
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    private static boolean worldValidC(World world) {
        return Minecraft.getMinecraft().world == world;
    }

    public static AxisAlignedBB getBoundingBox(Vec3d vec1, Vec3d vec2) {
        double minX = 0.0, minY = 0.0, minZ = 0.0, maxX = 0.0, maxY = 0.0, maxZ = 0.0;
        if(vec1.x < vec2.x) {
            minX = vec1.x;
            maxX = vec2.x;
        } else {
            minX = vec2.x;
            maxX = vec1.x;
        }
        if(vec1.y < vec2.y) {
            minY = vec1.y;
            maxY = vec2.y;
        } else {
            minY = vec2.y;
            maxY = vec1.y;
        }
        if(vec1.z < vec2.z) {
            minZ = vec1.z;
            maxZ = vec2.z;
        } else {
            minZ = vec2.z;
            maxZ = vec1.z;
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    /**
     * Return a minimum AABB that can hold the points given.
     */
    public static AxisAlignedBB minimumBounds(Vec3d ...points) {
        if(points.length == 0) {
            throw new RuntimeException("Invalid call: too few vectors");
        }

        double minX=points[0].x,maxX=points[0].x,minY=points[0].y,maxY=points[0].y,minZ=points[0].z,maxZ=points[0].z;
        for(int i = 1; i < points.length; ++i) {
            if(minX > points[i].x)
                minX = points[i].x;
            if(maxX < points[i].x)
                maxX = points[i].x;
            
            if(minY > points[i].y)
                minY = points[i].y;
            if(maxY < points[i].y)
                maxY = points[i].y;
            
            if(minZ > points[i].z)
                minZ = points[i].z;
            if(maxZ < points[i].z)
                maxZ = points[i].z;
        }
        
        return new AxisAlignedBB(minX, minY, minZ,maxX,maxY,maxZ);
    }
    
    public static List<BlockPos> getBlocksWithin(Entity entity, double range, int max, IBlockSelector ...filters) {
        return getBlocksWithin(entity.worldObj, entity.posX, entity.posY, entity.posZ, range, max, filters);
    }
    
    public static List<BlockPos> getBlocksWithin(TileEntity te, double range, int max, IBlockSelector ...filters) {
        return getBlocksWithin(te.getWorld(), te.getPos().getX() + 0.5, te.getPos().getY() + 0.5, te.getPos().getZ() + 0.5, range, max, filters);
    }
    
    public static List<BlockPos> getBlocksWithin(
            World world,
            final double x, final double y, final double z,
            double range, int max,
            IBlockSelector ...filter) {
        IBlockSelector [] fs = new IBlockSelector[filter.length + 1];
        for(int i = 0; i < filter.length; ++i)
            fs[i] = filter[i];
        
        final double rangeSq = range * range;
        
        fs[filter.length] = (world1, xx, yy, zz, block) ->
        {
            double dx = xx - x, dy = yy - y, dz = zz - z;
            return dx * dx + dy * dy + dz * dz <= rangeSq;
        };
        
        int minX = MathHelper.floor(x - range),
            minY = MathHelper.floor(y - range),
            minZ = MathHelper.floor(z - range),
            maxX = MathHelper.ceil(x + range),
            maxY = MathHelper.ceil(y + range),
            maxZ = MathHelper.ceil(z + range);
        
        return getBlocksWithin(world, minX, minY, minZ, maxX, maxY, maxZ, max, fs);
    }
    
    public static List<BlockPos> getBlocksWithin(
        World world,
        int minX, int minY, int minZ, 
        int maxX, int maxY, int maxZ, 
        int max, 
        IBlockSelector ...filter) {
        
        List<BlockPos> ret = new ArrayList();
        for(int x = minX; x <= maxX; ++x) {
            for(int y = minY; y <= maxY; ++y) {
                for(int z = minZ; z <= maxZ; ++z) {
                    boolean match = true;
                    for(IBlockSelector f : filter) {
                        if(!f.accepts(world, x, y, z, world.getBlockState(new BlockPos(x, y, z)).getBlock())) {
                            match = false;
                            break;
                        }
                    }
                    if(match) {
                        ret.add(new BlockPos(x, y, z));
                        if(ret.size() == max)
                            return ret;
                    }
                }
            }
        }
        
        return ret;
    }
    
    public static List<Entity> getEntities(TileEntity te, double range, Predicate<Entity> predicate) {
        return getEntities(te.getWorld(), te.getPos().getX() + 0.5, te.getPos().getY() + 0.5, te.getPos().getZ() + 0.5, range, predicate);
    }
    
    public static List<Entity> getEntities(Entity ent, double range, Predicate predicate) {
        return getEntities(ent.world, ent.posX, ent.posY, ent.posZ, range, predicate);
    }
    
    public static List<Entity> getEntities(World world, double x, double y, double z, double range,
                                           Predicate filter) {
        AxisAlignedBB box = new AxisAlignedBB(
            x - range, y - range, z - range, 
            x + range, y + range, z + range);
        return getEntities(world, box, input ->
        {
            Objects.requireNonNull(input);
            return EntitySelectors.within(x, y, z, range).apply(input) && filter.apply(input);
        });
    }

    @SuppressWarnings("unchecked")
    public static List<Entity> getEntities(World world, AxisAlignedBB box, Predicate predicate) {
        return world.getEntitiesWithinAABB(null, box, predicate);
    }
    
}
