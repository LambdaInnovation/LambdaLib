/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.lambdalib.annoreg.mc;

import cn.lambdalib.annoreg.base.RegistrationFieldSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.util.mc.SideHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.item.Item;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
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
