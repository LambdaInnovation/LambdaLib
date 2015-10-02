package cn.liutils.loading.block;

import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import cn.liutils.loading.Loader.ObjectNamespace;

public class DefaultRules {
	
	public static class UnlocalizedName extends BlockLoadRule {

		@Override
		public void load(Block block, ObjectNamespace ns, String name)
				throws Exception {
			String n = ns.getString("unlName");
			if(n != null) block.setBlockName(n);
		}
		
	}
	
	public static class Texture extends BlockLoadRule {

		@Override
		public void load(Block block, ObjectNamespace ns, String name)
				throws Exception {
			String n = ns.getString("textureName");
			if(n != null) block.setBlockTextureName(this.getNamespace(ns) + n);
		}
		
	}
	
	public static class StepSound extends BlockLoadRule {

		@Override
		public void load(Block block, ObjectNamespace ns, String name)
				throws Exception {
			SoundType ret = null;
			
			//TODO: We need a internal mapping for this will FAIL on deobf env.
			String s = ns.getString("soundType");
		    try {
		        ret = (SoundType) Block.class.getField("soundType" + s).get(null);
		        block.setStepSound(ret);
		    } catch(Exception e) {
		        e.printStackTrace();
		    }
		}
		
	}
	
	public static class LightLevel extends BlockLoadRule {

		@Override
		public void load(Block block, ObjectNamespace ns, String name)
				throws Exception {
			Float lightLevel = ns.getFloat("lightLevel");
			
			if(lightLevel != null) {
				block.setLightLevel(lightLevel);
			}
		}
		
	}
	
	public static class Hardness extends BlockLoadRule {

		@Override
		public void load(Block block, ObjectNamespace ns, String name)
				throws Exception {
			Float hardness = ns.getFloat("hardness");
			
			if(hardness != null) {
				block.setHardness(hardness);
			}
		}
		
	}
	
	public static class HarvestLevel extends BlockLoadRule {

		@Override
		public void load(Block block, ObjectNamespace ns, String name)
				throws Exception {
			String harvType = ns.getString("harvestType");
			Integer harvLevel = ns.getInt("harvestLevel");
			
			if(harvType != null && harvLevel != null) {
				block.setHarvestLevel(harvType, harvLevel);
			}
		}
		
	}
	
}
