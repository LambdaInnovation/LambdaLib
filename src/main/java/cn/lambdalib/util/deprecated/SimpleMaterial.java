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
package cn.lambdalib.util.deprecated;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.helper.Color;

/**
 * Most commonly used material that can almost handle all the situations within MC.
 * Supports light disabling, default alpha blending and pure-colored drawing.
 * @author WeAthFolD
 */
public class SimpleMaterial extends Material {
    
    public boolean ignoreLight = false;
    
    public SimpleMaterial(ResourceLocation _texture) {
        setTexture(_texture);
    }
    
    public SimpleMaterial setIgnoreLight() {
        ignoreLight = true;
        return this;
    }

    @Override
    public void onRenderStage(RenderStage stage) {
        if(stage == RenderStage.BEFORE_TESSELLATE) {
            GL11.glEnable(GL11.GL_BLEND);
            //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            color.bind();
            
            if(mainTexture != null) {
                RenderUtils.loadTexture(mainTexture);
            } else {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
            }
            
            if(ignoreLight) {
                GL11.glDisable(GL11.GL_LIGHTING);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            }
            
        } else if(stage == RenderStage.START_TESSELLATE) {
            if(ignoreLight) {
                Tessellator.instance.setBrightness(15728880);
            }
            
        } else if(stage == RenderStage.END) {
            if(ignoreLight) {
                GL11.glEnable(GL11.GL_LIGHTING);
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

}
