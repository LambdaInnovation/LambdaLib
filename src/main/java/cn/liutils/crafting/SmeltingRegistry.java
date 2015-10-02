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
package cn.liutils.crafting;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * @author WeAthFolD
 */
public class SmeltingRegistry implements IRecipeRegistry {
	
	public static final SmeltingRegistry INSTANCE = new SmeltingRegistry();
	
	private SmeltingRegistry() {}

	/* (non-Javadoc)
	 * @see cn.liutils.crafting.IRecipeRegistry#register(java.lang.String, net.minecraft.item.ItemStack, java.lang.Object[], int, int)
	 */
	@Override
	public void register(String type, ItemStack output, Object[] input,
			int width, int height, float experience) {
		if(width != 1 || height != 1) {
			throw new IllegalArgumentException("You can only specify 1 input for smelting!");
		}
		
		ItemStack in;
		if(input[0] instanceof String) {
			in = OreDictionary.getOres((String) input[0]).get(0);
		} else {
			in = (ItemStack) input[0];
		}
		
		GameRegistry.addSmelting(in, output, experience);
	}

}
