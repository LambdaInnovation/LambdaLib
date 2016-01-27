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
import cn.lambdalib.annoreg.core.RegistrationWithPostWork.PostWork;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author KSkun
 */
@RegistryTypeDecl
public class WorldGenRegistration extends RegistrationFieldSimple<RegWorldGen, IWorldGenerator> {

    public WorldGenRegistration() {
        super(RegWorldGen.class, "WorldGen");
        this.setLoadStage(LoadStage.PRE_INIT);
    }

    @Override
    protected void register(IWorldGenerator value, RegWorldGen anno, String field)
            throws Exception {
        GameRegistry.registerWorldGenerator(value, anno.value());
    }

}
