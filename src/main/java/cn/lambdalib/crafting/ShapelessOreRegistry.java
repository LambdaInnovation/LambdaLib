/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.crafting;

import com.google.common.base.Joiner;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * @author EAirPeter
 */
public class ShapelessOreRegistry implements IRecipeRegistry {

    public static final ShapelessOreRegistry INSTANCE = new ShapelessOreRegistry();

    @Override
    public void register(String type, ItemStack output, Object[] input, int width, int height, float experience) {
        String name = RecipeRegistry.reprStack(output) + " => " + Joiner.on(',').join(input);
        debug("[ShapelessOre] " +name);
        debug("type is"+input[0]);
        Ingredient[] ingredients=new Ingredient[input.length];
        for(int i=0;i<input.length;i++){
            ingredients[i]=Ingredient.fromItem((Item)input[i]);
        }
        GameRegistry.addShapelessRecipe(new ResourceLocation("lambda_lib:"+name),new ResourceLocation("lambda_lib"),output, ingredients);
    }

    private ShapelessOreRegistry() {}

}
