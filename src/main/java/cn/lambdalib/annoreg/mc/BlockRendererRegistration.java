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
