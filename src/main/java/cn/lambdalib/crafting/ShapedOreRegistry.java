/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.crafting;

import com.google.common.base.Joiner;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

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
            String spec = "";
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
                Joiner.on(',').join(recipe));
        GameRegistry.addRecipe(new ShapedOreRecipe(output, mirrored, recipe));
    }

    private ShapedOreRegistry() {
    }

}
