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
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@RegistryTypeDecl
public class ItemRegistration extends RegistrationFieldSimple<RegItem, Item> {

    public ItemRegistration() {
        super(RegItem.class, "Item");
        this.setLoadStage(LoadStage.PRE_INIT);
        
        this.addWork(RegItem.OreDict.class, new PostWork<RegItem.OreDict, Item>() {
            @Override
            public void invoke(RegItem.OreDict anno, Item obj) throws Exception {
                OreDictionary.registerOre(anno.value(), obj);
            }
        });
        
        this.addWork(RegItem.UTName.class, new PostWork<RegItem.UTName, Item>() {
            @Override
            public void invoke(RegItem.UTName anno, Item obj) throws Exception {
                obj.setUnlocalizedName(getCurrentMod().getPrefix() + anno.value());
                obj.setTextureName(getCurrentMod().getRes(anno.value()));
            }
        });

        if (SideHelper.isClient()) {
            this.addWork(RegItem.HasRender.class, new PostWork<RegItem.HasRender, Item>() {
                @Override
                @SideOnly(Side.CLIENT)
                public void invoke(RegItem.HasRender anno, Item obj) throws Exception {
                    MinecraftForgeClient.registerItemRenderer(obj,
                            (IItemRenderer) helper.getFieldFromObject(obj, RegItem.Render.class));
                }
            });
        }
    }

    @Override
    protected void register(Item value, RegItem anno, String field) throws Exception {
        GameRegistry.registerItem(value, getSuggestedName());
    }
}
