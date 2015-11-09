package cn.lambdalib.util.mc;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BlockSelectors {
	
	public static final IBlockSelector filNothing = new IBlockSelector() {

		@Override
		public boolean accepts(World world, int x, int y, int z, Block block) {
			return block != Blocks.air;
		}
		
	},
	
	filNormal = new IBlockSelector() {

		@Override
		public boolean accepts(World world, int x, int y, int z, Block block) {
			Block b = world.getBlock(x, y, z);
			return b.getCollisionBoundingBoxFromPool(world, x, y, z) != null && 
					b.canCollideCheck(world.getBlockMetadata(x, y , z), false);
		}
		
	},
	
	filEverything = new IBlockSelector() {
		
		@Override
		public boolean accepts(World world, int x, int y, int z, Block block) {
			return false;
		}
		
	};
	
	

}
