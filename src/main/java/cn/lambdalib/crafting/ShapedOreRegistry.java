/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.crafting;

import com.google.common.base.Joiner;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
                    recipe[_i++] = (char) (index + 'A');
                    recipe[_i++] = input[index];
                } else
                    spec += ' ';
            recipe[y] = spec;
        }
        String name=RecipeRegistry.reprStack(output) + "[" + mirrored + "]" +
                Joiner.on(',').join(recipe);
        debug("[ShapedOre] " +name+"\n");
        GameRegistry.addShapedRecipe(new ResourceLocation("lambda_lib:"+name), new ResourceLocation("lambda_lib"),output, recipe);
    }

    private ShapedOreRegistry() {
    }

}
