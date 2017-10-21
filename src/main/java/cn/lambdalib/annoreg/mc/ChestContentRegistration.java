/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

import cn.lambdalib.util.mc.LILootEntry;
import cn.lambdalib.util.mc.LILootManager;
import net.minecraft.item.Item;
import cn.lambdalib.annoreg.base.RegistrationFieldSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;

/**
 * @author KSkun, Paindar
 */
@RegistryTypeDecl
public class ChestContentRegistration extends RegistrationFieldSimple<RegChestContent, Item> {

    public ChestContentRegistration() {
        super(RegChestContent.class, "ChestContent");
        this.setLoadStage(LoadStage.INIT);
    }

    @Override
    protected void register(Item value, RegChestContent anno, String field)
            throws Exception {
        LILootEntry entry=new LILootEntry(value,anno.size(),anno.prob(),String.format("LI%s_%d_%d",value.getUnlocalizedName(),anno.prob(),anno.size()));
        for(int i : anno.value()) {
            LILootManager.getInstance().add(type(i),entry);
        }
    }

    private final String[] types= new String[]{"spawn_bonus_chest" ,
            "end_city_treasure" ,
            "imple_dungeon" ,
            "village_blacksmith" ,
            "abandoned_mineshaft" ,
            "nether_bridge" ,
            "stronghold_library" ,
            "stronghold_crossing" ,
            "stronghold_corridor" ,
            "desert_pyramid" ,
            "jungle_temple" ,
            "jungle_temple_dispenser" ,
            "igloo_chest" ,
            "woodland_mansion"};
    private String type(int type) {
        return type>=types.length||type<0?"unknown":types[type];
    }

}
