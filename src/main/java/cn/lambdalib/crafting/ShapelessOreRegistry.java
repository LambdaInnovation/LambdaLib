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
import net.minecraftforge.oredict.ShapelessOreRecipe;

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
