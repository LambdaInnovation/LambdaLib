/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

import cn.lambdalib.annoreg.base.RegistrationFieldSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.util.mc.SideHelper;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;


/**
 * Register all item marked by {@anno RegItem}.
 * @author acaly
 *
 * But now MC have {@event registerBlocks}, so will we keep it continually?
 * @author Paindar
 */
@RegistryTypeDecl
public class ItemRegistration extends RegistrationFieldSimple<RegItem, Item> {

    public ItemRegistration() {
        super(RegItem.class, "Item");
        this.setLoadStage(LoadStage.PRE_INIT);
        
        this.addWork(RegItem.OreDict.class, (PostWork<RegItem.OreDict, Item>) (anno, obj) -> OreDictionary.registerOre(anno.value(), obj));
        
        this.addWork(RegItem.UTName.class, (PostWork<RegItem.UTName, Item>) (anno, obj) -> {
            obj.setUnlocalizedName(getCurrentMod().getPrefix() + anno.value());
            //TODO add Texture path
            //obj.setTextureName(getCurrentMod().getRes(anno.value()));
        });

        if (SideHelper.isClient()) {
            this.addWork(RegItem.HasRender.class, new PostWork<RegItem.HasRender, Item>() {
                @Override
                @SideOnly(Side.CLIENT)
                public void invoke(RegItem.HasRender anno, Item obj) throws Exception {
                    //TODO add ItemRenderer.
                    //MinecraftForgeClient.registerItemRenderer(obj, (IItemRenderer) helper.getFieldFromObject(obj, RegItem.Render.class));
                }
            });
        }
    }

    @Override
    protected void register(Item value, RegItem anno, String field) throws Exception {
        value.setUnlocalizedName(getSuggestedName());
        GameRegistry.findRegistry(Item.class).register(value);
    }
}
