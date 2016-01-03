package cn.lambdalib.crafting;

import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.generic.DebugUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author EAirPeter
 */
public class ShapedOreRegistry implements IRecipeRegistry {

    public static final ShapedOreRegistry INSTANCE = new ShapedOreRegistry();

    @Override
    public void register(String type, ItemStack output, Object[] input, int width, int height, float experience) {
        boolean mirrored = !type.equals("shaped_s");
        int pairs = 0;
        for (Object elem : input)
            if (elem != null)
                ++pairs;
        Object[] recipe = new Object[height + pairs * 2];
        int index = 0;
        int _i = height;
        for (int y = 0; y < height; ++y) {
            String spec = new String();
            for (int x = 0; x < width; ++x, ++index)
                if (input[index] != null) {
                    spec += (char) (index + 'A');
                    recipe[_i++] = Character.valueOf((char) (index + 'A'));
                    recipe[_i++] = input[index];
                } else
                    spec += ' ';
            recipe[y] = spec;
        }

        debug("[ShapedOre] " +
                RecipeRegistry.reprStack(output) + "[" + mirrored + "]" +
                DebugUtils.formatArray(recipe));
        GameRegistry.addRecipe(new ShapedOreRecipe(output, mirrored, recipe));
    }

    private ShapedOreRegistry() {
    }

}
