/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.multiblock;

import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.WEST;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.template.client.render.block.RenderEmptyBlock;
import cn.lambdalib.util.generic.VecUtils;
import cn.lambdalib.util.mc.WorldUtils;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author WeathFolD
 */
public abstract class BlockMulti extends BlockContainer {

    List<SubBlockPos> subList = new ArrayList();
    List<SubBlockPos>[] buffer;

    private AxisAlignedBB[] renderBB = new AxisAlignedBB[8];

    @SideOnly(Side.CLIENT)
    double[][] rotCenters;

    boolean init = false;

    public static class SubBlockPos {
        public final int dx, dy, dz;

        public SubBlockPos(int _dx, int _dy, int _dz) {
            dx = _dx;
            dy = _dy;
            dz = _dz;
        }
    }

    /**
     * notice that you must call finishInit() in your own subclass ctor.
     */
    public BlockMulti(Material p_i45386_1_) {
        super(p_i45386_1_);
        addSubBlock(0, 0, 0);
    }

    public void addSubBlock(int dx, int dy, int dz) {
        if (init) {
            throw new RuntimeException("Trying to add a sub block after block init finished");
        }
        subList.add(new SubBlockPos(dx, dy, dz));
    }

    /**
     * Accept a int[][3] array, add all the array element as a single subPos
     * inside the list.
     */
    public void addSubBlock(int[][] data) {
        for (int[] s : data) {
            addSubBlock(s[0], s[1], s[2]);
        }
    }

    /**
     * Get the render bounding box of this BlockMulti at the given block
     * position (as origin block) Usually used on TileEntity rendering.
     */
    public AxisAlignedBB getRenderBB(int x, int y, int z, EnumFacing dir) {
        // Lazy init
        if (renderBB[dir.ordinal()] == null) {
            Vec3[] vecs = new Vec3[subList.size() * 2];
            for (int i = 0; i < subList.size(); ++i) {
                SubBlockPos rot = rotate(subList.get(i), dir);
                vecs[i * 2] = VecUtils.vec(rot.dx, rot.dy, rot.dz);
                vecs[i * 2 + 1] = VecUtils.vec(rot.dx + 1, rot.dy + 1, rot.dz + 1);
            }

            renderBB[dir.ordinal()] = WorldUtils.minimumBounds(vecs);
        }

        AxisAlignedBB box = renderBB[dir.ordinal()];
        return new AxisAlignedBB(box.minX + x, box.minY + y, box.minZ + z, box.maxX + x, box.maxY + y,
                box.maxZ + z);
    }

    /**
     * You MUST call this via your ctor, after init all the blocks.
     */
    public void finishInit() {
        // Pre-init rotated position offset list.
        buffer = new ArrayList[6];

        for (int i = 2; i <= 5; ++i) {
            EnumFacing dir = EnumFacing.values()[i];
            buffer[i] = new ArrayList();
            for (SubBlockPos s : subList) {
                buffer[i].add(rotate(s, dir));
            }
        }

        if (FMLCommonHandler.instance().getSide().isClient()) {
            double[] arr = getRotCenter();
            rotCenters = new double[][] { {}, {}, { arr[0], arr[1], arr[2] }, { -arr[0], arr[1], -arr[2] },
                    { arr[2], arr[1], -arr[0] }, { -arr[2], arr[1], arr[0] } };
        }

        // Finished, set the flag and encapsulate the instance.
        init = true;
    }

    // Rotation API
    // Some lookup tables
    private static final EnumFacing[] rotMap = {
        NORTH, // -Z,
        EAST, // +X,
        SOUTH, // +Z,
        WEST // -X
    };
    
    private static final double[] drMap = { 0, 0, 180, 0, -90, 90 };
    private static final double[][] offsetMap = { { 0, 0 }, // placeholder
            { 0, 0 }, // placeholder
            { 0, 0 }, { 1, 1 }, { 0, 1 }, { 1, 0 } };

    public double[] getPivotOffset(InfoBlockMulti info) {
        return getPivotOffset(info.dir);
    }

    public EnumFacing getRotation(int l) {
        return rotMap[l];
    }

    /**
     * Get the whole structure's (minX, minZ) point coord, in [dir = 0] (a.k.a:
     * facing z-) point of view.
     * 
     * @param dir
     * @return
     */
    public double[] getPivotOffset(EnumFacing dir) {
        return offsetMap[dir.ordinal()];
    }

    @SideOnly(Side.CLIENT)
    public abstract double[] getRotCenter();

    /**
     * Build a multiblock at the given coordinate.
     */
    @Deprecated
    public void setMultiBlock(World world, int x, int y, int z, EnumFacing dir) {
        BlockPos pos=new BlockPos(x,y,z);
        setMultiBlock(world,pos,dir);
    }

    public void setMultiBlock(World world, BlockPos pos, EnumFacing dir) {
        world.setBlockToAir(pos);
        world.setBlockState(pos, this.getDefaultState());
        updateDirInfo(world, pos, dir);
    }

    // Placement API
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (world.isRemote)
            return;

        int l = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        EnumFacing dir = rotMap[l];
        updateDirInfo(world, x, y, z, dir);
    }

    @Deprecated
    private void updateDirInfo(World world, int x, int y, int z, EnumFacing dir) {
        // Set the origin block.
        BlockPos pos=new BlockPos(x,y,z);
        updateDirInfo(world,pos,dir);
    }

    private void updateDirInfo(World world, BlockPos pos, EnumFacing dir) {
        // Set the origin block.
        TileEntity te = world.getTileEntity(pos);
        ((IMultiTile) te).setBlockInfo(new InfoBlockMulti(te, dir, 0));

        List<SubBlockPos> rotatedList = buffer[dir.ordinal()];
        // Check done in ItemBlockMulti, brutely replace here.
        for (int i = 1; i < rotatedList.size(); ++i) {
            SubBlockPos sub = rotatedList.get(i);
            BlockPos subPos=pos.add(sub.dx,sub.dy,sub.dz);
            world.setBlockState(subPos, this.getDefaultState());
            te = world.getTileEntity(subPos);
            ((IMultiTile) te).setBlockInfo(new InfoBlockMulti(te, dir, i));
        }
    }

    @Deprecated
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        if (world.isRemote)
            return;

        BlockPos pos=new BlockPos(x,y,z);
        breakBlock(world,pos,block,metadata);
    }

    public void breakBlock(World world, BlockPos position, Block block, int metadata) {
        if (world.isRemote)
            return;

        TileEntity te = world.getTileEntity(position);
        if (!(te instanceof IMultiTile)) {
            LambdaLib.log.error("Didn't find correct tile when breaking a BlockMulti.");
            return;
        }
        InfoBlockMulti info = ((IMultiTile) te).getBlockInfo();
        int[] origin = getOrigin(te);
        if (origin == null)
            return;

        List<SubBlockPos> rotatedList = buffer[info.dir.ordinal()];
        for (SubBlockPos pos : rotatedList) {
            world.setBlockToAir(new BlockPos(origin[0] + pos.dx, origin[1] + pos.dy, origin[2] + pos.dz));
        }
    }

    // A series of getOrigin funcs.

    @Deprecated
    public int[] getOrigin(World world, int x, int y, int z) {
        return getOrigin(world.getTileEntity(new BlockPos(x, y, z)));
    }
    public int[] getOrigin(World world, BlockPos pos) {
        return getOrigin(world.getTileEntity(pos));
    }

    public int[] getOrigin(TileEntity te) {
        TileEntity ret = getOriginTile(te);
        return ret == null ? null : new int[] { ret.getPos().getX(), ret.getPos().getY(), ret.getPos().getZ() };
    }

    @Deprecated
    public TileEntity getOriginTile(World world, int x, int y, int z) {
        return getOriginTile(world,new BlockPos(x, y, z));
    }

    public TileEntity getOriginTile(World world, BlockPos pos) {
        TileEntity now = world.getTileEntity(pos);
        return getOriginTile(now);
    }

    public TileEntity getOriginTile(TileEntity now) {
        if (!(now instanceof IMultiTile)) {
            return null;
        }
        InfoBlockMulti info = ((IMultiTile) now).getBlockInfo();
        if (info == null || !info.isLoaded())
            return null;
        SubBlockPos sbp = buffer[info.dir.ordinal()].get(info.subID);
        return validate(
                now.getWorld().getTileEntity(now.getPos().add(- sbp.dx, now.getPos().getY() - sbp.dy, now.getPos().getZ() - sbp.dz)));
    }

    // Internal
    public static final SubBlockPos rotate(SubBlockPos s, EnumFacing dir) {
        switch (dir) {
        case EAST:
            return new SubBlockPos(-s.dz, s.dy, s.dx);
        case WEST:
            return new SubBlockPos(s.dz, s.dy, -s.dx);
        case SOUTH:
            return new SubBlockPos(-s.dx, s.dy, -s.dz);
        case NORTH:
            return new SubBlockPos(s.dx, s.dy, s.dz);
        default:
            throw new RuntimeException("Invalid rotate direction");
        }
    }

    double getRotation(InfoBlockMulti info) {
        return drMap[info.dir.ordinal()];
    }

    private TileEntity validate(TileEntity te) {
        return te instanceof IMultiTile ? te : null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderType() {
        return RenderEmptyBlock.id;
    }

}
