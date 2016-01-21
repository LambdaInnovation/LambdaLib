package cn.lambdalib.crafting;

import com.google.common.base.Joiner;
import net.minecraft.item.ItemStack;
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
                Joiner.on(',').join(input));
    }

    private ShapelessOreRegistry() {}

}
