/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import net.minecraft.util.ResourceLocation;
import cn.lambdalib.util.helper.Color;

/**
 * @author WeAthFolD
 *
 */
public abstract class Material {
    
    public ResourceLocation mainTexture;
    public Color color = new Color();

    public abstract void onRenderStage(RenderStage stage);
    
    public Material setTexture(ResourceLocation tex) {
        mainTexture = tex;
        return this;
    }
    
}
