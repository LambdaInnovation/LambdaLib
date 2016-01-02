package cn.lambdalib.crafting;

import java.lang.reflect.Constructor;

import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.generic.DebugUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author EAirPeter
 */
public class ShapelessOreRegistry implements IRecipeRegistry {

    public static final ShapelessOreRegistry INSTANCE = new ShapelessOreRegistry();

    @Override
    public void register(String type, ItemStack output, Object[] input, int width, int height, float experience) {
        GameRegistry.addRecipe(new ShapelessOreRecipe(output, input));

        debug("[ShapelessOre] " +
                RecipeRegistry.reprStack(output) + " => " +
                DebugUtils.formatArray(input));
    }

    private ShapelessOreRegistry() {}

}
