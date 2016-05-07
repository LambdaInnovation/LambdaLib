/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.mc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cn.lambdalib.util.helper.BlockPos;
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
        return Minecraft.getMinecraft().theWorld == world;
    }

    public static AxisAlignedBB getBoundingBox(Vec3 vec1, Vec3 vec2) {
        double minX = 0.0, minY = 0.0, minZ = 0.0, maxX = 0.0, maxY = 0.0, maxZ = 0.0;
        if(vec1.xCoord < vec2.xCoord) {
            minX = vec1.xCoord;
            maxX = vec2.xCoord;
        } else {
            minX = vec2.xCoord;
            maxX = vec1.xCoord;
        }
        if(vec1.yCoord < vec2.yCoord) {
            minY = vec1.yCoord;
            maxY = vec2.yCoord;
        } else {
            minY = vec2.yCoord;
            maxY = vec1.yCoord;
        }
        if(vec1.zCoord < vec2.zCoord) {
            minZ = vec1.zCoord;
            maxZ = vec2.zCoord;
        } else {
            minZ = vec2.zCoord;
            maxZ = vec1.zCoord;
        }
        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    /**
     * Return a minimum AABB that can hold the points given.
     */
    public static AxisAlignedBB minimumBounds(Vec3 ...points) {
        if(points.length == 0) {
            throw new RuntimeException("Invalid call: too few vectors");
        }
        AxisAlignedBB ret = AxisAlignedBB.getBoundingBox(
            points[0].xCoord, points[0].yCoord, points[0].zCoord, 
            points[0].xCoord, points[0].yCoord, points[0].zCoord);
        
        for(int i = 1; i < points.length; ++i) {
            if(ret.minX > points[i].xCoord)
                ret.minX = points[i].xCoord;
            if(ret.maxX < points[i].xCoord)
                ret.maxX = points[i].xCoord;
            
            if(ret.minY > points[i].yCoord)
                ret.minY = points[i].yCoord;
            if(ret.maxY < points[i].yCoord)
                ret.maxY = points[i].yCoord;
            
            if(ret.minZ > points[i].zCoord)
                ret.minZ = points[i].zCoord;
            if(ret.maxZ < points[i].zCoord)
                ret.maxZ = points[i].zCoord;
        }
        
        return ret;
    }
    
    public static List<BlockPos> getBlocksWithin(Entity entity, double range, int max, IBlockSelector ...filters) {
        return getBlocksWithin(entity.worldObj, entity.posX, entity.posY, entity.posZ, range, max, filters);
    }
    
    public static List<BlockPos> getBlocksWithin(TileEntity te, double range, int max, IBlockSelector ...filters) {
        return getBlocksWithin(te.getWorldObj(), te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5, range, max, filters);
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
        
        fs[filter.length] = new IBlockSelector() {

            @Override
            public boolean accepts(World world, int xx, int yy, int zz, Block block) {
                double dx = xx - x, dy = yy - y, dz = zz - z;
                return dx * dx + dy * dy + dz * dz <= rangeSq;
            }
            
        };
        
        int minX = MathHelper.floor_double(x - range),
            minY = MathHelper.floor_double(y - range),
            minZ = MathHelper.floor_double(z - range),
            maxX = MathHelper.ceiling_double_int(x + range),
            maxY = MathHelper.ceiling_double_int(y + range),
            maxZ = MathHelper.ceiling_double_int(z + range);
        
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
                        if(!f.accepts(world, x, y, z, world.getBlock(x, y, z))) {
                            match = false;
                            break;
                        }
                    }
                    if(match) {
                        ret.add(new BlockPos(world, x, y, z));
                        if(ret.size() == max)
                            return ret;
                    }
                }
            }
        }
        
        return ret;
    }
    
    public static List<Entity> getEntities(TileEntity te, double range, Predicate<Entity> predicate) {
        return getEntities(te.getWorldObj(), te.xCoord + 0.5, te.yCoord + 0.5, te.zCoord + 0.5, range, predicate);
    }
    
    public static List<Entity> getEntities(Entity ent, double range, Predicate<Entity> predicate) {
        return getEntities(ent.worldObj, ent.posX, ent.posY, ent.posZ, range, predicate);
    }
    
    public static List<Entity> getEntities(World world, double x, double y, double z, double range,
                                           Predicate<Entity> filter) {
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
            x - range, y - range, z - range, 
            x + range, y + range, z + range);
        return getEntities(world, box, EntitySelectors.within(x, y, z, range).and(filter));
    }

    @SuppressWarnings("unchecked")
    public static List<Entity> getEntities(World world, AxisAlignedBB box, Predicate<Entity> predicate) {
        return world.getEntitiesWithinAABBExcludingEntity(null, box, EntitySelectors.toEntitySelector(predicate));
    }
    
}
