/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import cn.lambdalib.annoreg.base.RegistrationFieldSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;

/**
 * @author KSkun
 */
@RegistryTypeDecl
public class ChestContentRegistration extends RegistrationFieldSimple<RegChestContent, WeightedRandomChestContent> {
    
    public ChestContentRegistration() {
        super(RegChestContent.class, "ChestContent");
        this.setLoadStage(LoadStage.INIT);
    }

    @Override
    protected void register(WeightedRandomChestContent value, RegChestContent anno, String field) 
            throws Exception {
        for(int i : anno.value()) {
            ChestGenHooks.addItem(type(i), value);
        }
    }
    
    private String type(int type) {
        switch(type) {
        case 0:
            return "dungeonChest";
        case 1:
            return "villageBlacksmith";
        case 2:
            return "pyramidDesertyChest";
        case 3:
            return "pyramidJungleChest";
        case 4:
            return "mineshaftCorridor";
        case 5:
            return "pyramidJungleDispenser";
        case 6:
            return "strongholdCorridor";
        case 7:
            return "strongholdLibrary";
        case 8:
            return "strongholdCrossing";
        case 9:
            return "bonusChest";
        }
        return null;
    }

}
