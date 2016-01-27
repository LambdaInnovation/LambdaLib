/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

import net.minecraft.block.Block;
import net.minecraftforge.oredict.OreDictionary;
import cn.lambdalib.annoreg.base.RegistrationFieldSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegModInformation;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cpw.mods.fml.common.registry.GameRegistry;

@RegistryTypeDecl
public class BlockRegistration extends RegistrationFieldSimple<RegBlock, Block> {

    public BlockRegistration() {
        super(RegBlock.class, "Block");
        this.setLoadStage(LoadStage.PRE_INIT);
        
        this.addWork(RegBlock.OreDict.class, new PostWork<RegBlock.OreDict, Block>() {
            @Override
            public void invoke(RegBlock.OreDict anno, Block obj) throws Exception {
                OreDictionary.registerOre(anno.value(), obj);
            }
        });
        
        this.addWork(RegBlock.BTName.class, new PostWork<RegBlock.BTName, Block>() {
            @Override
            public void invoke(RegBlock.BTName anno, Block obj) throws Exception {
                obj.setBlockName(getCurrentMod().getPrefix() + anno.value());
                obj.setBlockTextureName(getCurrentMod().getRes(anno.value()));
            }
        });
        
    }

    @Override
    protected void register(Block value, RegBlock anno, String field) throws Exception {
        GameRegistry.registerBlock(value, anno.item(), getSuggestedName());
    }

}
