package cn.liutils.crafting;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.commons.io.IOUtils;

import cn.lambdalib.core.LambdaLib;
import cn.liutils.util.generic.DebugUtils;
import cn.liutils.util.generic.RegistryUtils;

/**
 * @author EAirPeter, WeAthFolD
 */
public class RecipeRegistry {
	
	/**
	 * This should only be visited for debug purpose.
	 */
	public final Map<String, Object> nameMapping = new HashMap();
	
	public RecipeRegistry() {
		registerRecipeType("shaped", ShapedOreRegistry.INSTANCE);
		registerRecipeType("shaped_s", ShapedOreRegistry.INSTANCE);
		registerRecipeType("shapeless", ShapelessOreRegistry.INSTANCE);
		registerRecipeType("smelting", SmeltingRegistry.INSTANCE);
	}
	
	/**
	 * Map a custom string specified in the recipe file to be a specific type of object.
	 * The object can be an Item, a Block or a String (in oreDict).
	 */
	public void map(String key, Object obj) {
		if(!(obj instanceof Item) && !(obj instanceof Block) && !(obj instanceof String))
			throw new RuntimeException("Invalid object to map " + key + "=>" + obj);
		
		nameMapping.put(key, obj);
	}
	
	private Object getNamedObject(String key) {
		if(key.equals("nil"))
			return null;
		
		Object o = nameMapping.get(key);
		if(o != null)
			return o;
		
		if(OreDictionary.doesOreNameExist(key))
			return key;
		
		if(Item.itemRegistry.containsKey(key))
			return Item.itemRegistry.getObject(key);
		if(Block.blockRegistry.containsKey(key))
			return Block.blockRegistry.getObject(key);
		
		throw new RuntimeException("Registry object " + key + " doesn't exist");
	}
	
	private Object getRegistryObject(ParsedRecipeElement element) {
		Object o = getNamedObject(element.name);
		if(o == null)
			return null;
		// if we didn't get the data value, we suggest the user is trying to specify a recipe for all subtypes, therefore return the direct object,
		// 	rather than creating an ItemStack
		if(!element.dataParsed)
			return o;
		if(o instanceof Item) {
			return new ItemStack((Item) o, element.amount, element.data);
		} else if(o instanceof Block) {
			return new ItemStack((Block) o, element.amount, element.data);
		} else {
			return (String) o;
		}
	}
	
	private ItemStack getOutputObject(ParsedRecipeElement element) {
		Object o = getNamedObject(element.name);
		if(o == null)
			throw new RuntimeException("Registry object " + element.name + " can't be nil");
		if(o instanceof Item) {
			return new ItemStack((Item) o, element.amount, element.data);
		} else if(o instanceof Block) {
			return new ItemStack((Block) o, element.amount, element.data);
		} else {
			ItemStack outputstack=OreDictionary.getOres(element.name).get(0).copy();
			outputstack.stackSize=element.amount;
			return outputstack;
		}
	}
	
	/**
	 * Assign a registry to the type given
	 * @param type The type
	 * @param registry The registry
	 */
	public void registerRecipeType(String type, IRecipeRegistry registry) {
		if (map.containsKey(type))
			throw new IllegalArgumentException("Recipe type \"" + type + "\" exists");
		map.put(type, registry);
	}
	
	/**
	 * Add all recipes from a file. The recipe's type must be registered before.
	 * @param path The path of the file containing recipes
	 */
	public void addRecipeFromFile(String path) {
		addRecipeFromFile(new File(path));
	}
	
	/**
	 * Add all recipes from a file. The recipe's type must be registered before.
	 * @param file The file containing recipes
	 */
	public void addRecipeFromFile(File file) {
		RecipeParser parser = null;
		try {
			parser = new RecipeParser(file);
			addRecipe(parser);
		}
		catch (Throwable e) {
			LambdaLib.log.error("Failed to load recipes from file: " + file, e);
		}
		finally {
			parser.close();
		}
	}
	
	public void addRecipeFromResourceLocation(ResourceLocation src) {
		try {
			addRecipeFromString(IOUtils.toString(RegistryUtils.getResourceStream(src)));
		} catch(Throwable e) {
			LambdaLib.log.error("Failed to load recipes from file: " + src, e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Add all recipes from a string. The recipe's type must be registered before.
	 * @param recipes The string specifying recipes
	 */
	public void addRecipeFromString(String recipes) {
		RecipeParser parser = null;
		try {
			parser = new RecipeParser(recipes);
			addRecipe(parser);
		} catch (Throwable e) {
			LambdaLib.log.error("Failed to load recipes from String: " + recipes, e);
		}
		finally {
			parser.close();
		}
	}
	
	private void addRecipe(RecipeParser parser) {
		while (parser.parseNext()) {
			try {
				String type = parser.getType();
				IRecipeRegistry registry = map.get(type);
				if (registry != null) {
					ParsedRecipeElement[] parsed = parser.getInput();
					Object[] input = new Object[parsed.length];
					for(int i = 0; i < input.length; ++i) {
						input[i] = getRegistryObject(parsed[i]);
					}
					//System.out.println(DebugUtils.formatArray(input));
					
					registry.register(type, getOutputObject(parser.getOutput()), input,
						parser.getWidth(), parser.getHeight(), parser.getExperience());
				}
				else
					LambdaLib.log.error("Failed to register a recipe because the type \"" + type + "\" doesn't have its registry");
			} catch(Exception e) {
				LambdaLib.log.error("Failed processing one recipe element", e);
			}
		}
	}
	
	private HashMap<String, IRecipeRegistry> map = new HashMap<String, IRecipeRegistry>();
	
}
