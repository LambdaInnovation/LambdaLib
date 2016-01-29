/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

import cn.lambdalib.annoreg.base.RegistrationInstance;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

/**
 * @author WeAthFolD
 */
@RegistryTypeDecl
public class BlockRendererRegistration extends
        RegistrationInstance<RegBlockRenderer, ISimpleBlockRenderingHandler> {

    public BlockRendererRegistration() {
        super(RegBlockRenderer.class, "BlockRenderer");
        this.setLoadStage(LoadStage.INIT);
    }

    @Override
    protected void register(ISimpleBlockRenderingHandler obj,
            RegBlockRenderer anno) throws Exception {
        RenderingRegistry.registerBlockHandler(obj);
    }

}
