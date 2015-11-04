package cn.lambdalib.util.mc;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockFilters {
	
	public static final IBlockFilter filNothing = new IBlockFilter() {

		@Override
		public boolean accepts(World world, int x, int y, int z, Block block) {
			return block != Blocks.air;
		}
		
	},
	
	filNormal = new IBlockFilter() {

		@Override
		public boolean accepts(World world, int x, int y, int z, Block block) {
			Block b = world.getBlock(x, y, z);
			return b.getCollisionBoundingBoxFromPool(world, x, y, z) != null && 
					b.canCollideCheck(world.getBlockMetadata(x, y , z), false);
		}
		
	},
	
	filEverything = new IBlockFilter() {
		
		@Override
		public boolean accepts(World world, int x, int y, int z, Block block) {
			return false;
		}
		
	};
	
	

}
