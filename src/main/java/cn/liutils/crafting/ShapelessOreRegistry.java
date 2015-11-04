package cn.liutils.crafting;

import java.lang.reflect.Constructor;

import cn.lambdalib.core.LambdaLib;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author EAirPeter
 */
public class ShapelessOreRegistry implements IRecipeRegistry {

	public static final ShapelessOreRegistry INSTANCE = new ShapelessOreRegistry();
	private static Constructor<ShapelessOreRecipe> ctor = null;
	
	static {
		try {
			ctor = ShapelessOreRecipe.class.getConstructor(ItemStack.class, Object[].class);
		}
		catch(Throwable e) {
			throw new RuntimeException("Failed to get the constructor of class \"ShapelessOreRecipe\"", e);
		}
	}
	
	@Override
	public void register(String type, ItemStack output, Object[] input, int width, int height, float experience) {
		try {
			Object[] recipe = new Object[input.length];
			for (int i = 0; i < input.length; ++i)
				recipe[i] = input[i];
			GameRegistry.addRecipe(ctor.newInstance(output, recipe));
		} catch (Throwable e) {
			LambdaLib.log.error("Failed to register a recipe", e);
		}
	}

	private ShapelessOreRegistry() {
	}
	
}
