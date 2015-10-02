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
package cn.liutils.template.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cn.liutils.template.block.BlockMulti.SubBlockPos;

/**
 * @author WeathFolD
 */
public class ItemBlockMulti extends ItemBlock {

	/**
	 * @param block
	 */
	public ItemBlockMulti(Block block) {
		super(block);
	}
	
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
    		float rx, float ry, float rz) {
        Block block = world.getBlock(x, y, z);

        if (block == Blocks.snow_layer && (world.getBlockMetadata(x, y, z) & 7) < 1) {
            side = 1;
        } else if(block != Blocks.vine && 
        	block != Blocks.tallgrass && 
        	block != Blocks.deadbush && 
        	!block.isReplaceable(world, x, y, z)) {
            if (side == 0) --y;
            if (side == 1) ++y;
            if (side == 2) --z;
            if (side == 3) ++z;
            if (side == 4) --x;
            if (side == 5) ++x;
        }

        if (stack.stackSize == 0) return false;
        if (!player.canPlayerEdit(x, y, z, side, stack)) return false;
        if (y == 255 && this.field_150939_a.getMaterial().isSolid()) return false;
        if (world.canPlaceEntityOnSide(this.field_150939_a, x, y, z, false, side, player, stack)) {
            int i1 = this.getMetadata(stack.getItemDamage());
            int j1 = this.field_150939_a.onBlockPlaced(world, x, y, z, side, rx, ry, rz, i1);

            //Further validation with BlockMulti logic
            BlockMulti bm = (BlockMulti) this.field_150939_a;
            int l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            List<SubBlockPos> list = bm.buffer[bm.getRotation(l).ordinal()];
            for(SubBlockPos s : list) {
            	Block t = world.getBlock(x + s.dx, y + s.dy, z + s.dz);
            	if(!t.isReplaceable(world, x, y, z)) {
            		return false;
            	}
            }
            
            if (placeBlockAt(stack, player, world, x, y, z, side, rx, ry, rz, j1))
            {
                world.playSoundEffect((double)((float)x + 0.5F), (double)((float)y + 0.5F), 
                		(double)((float)z + 0.5F), 
                		this.field_150939_a.stepSound.func_150496_b(), 
                		(this.field_150939_a.stepSound.getVolume() + 1.0F) / 2.0F, 
                		this.field_150939_a.stepSound.getPitch() * 0.8F);
                --stack.stackSize;
            }

            return true;
        }
        return false;
    }

}
