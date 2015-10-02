package cn.liutils.loading.block;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import cn.liutils.loading.Loader.ObjectNamespace;

public abstract class BlockLoadRule<T extends Block> {
	
	public abstract void load(T block, ObjectNamespace ns, String name) throws Exception;
	
	public void finishedLoad(T block, ObjectNamespace ns, String name) throws Exception {}
	
	public boolean applyFor(Block block, BlockLoader loader, String name) {
		return true;
	}
	
	protected String getNamespace(ObjectNamespace ns) {
		String name = ns.getString("namespace");
		return name == null ? "minecraft" : name;
	}
	
}
