/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.mc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import cn.lambdalib.annoreg.core.AnnotationData;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryType;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author WeathFolD
 */
@RegistryTypeDecl
@SideOnly(Side.CLIENT)
public class PreloadTexRegistration extends RegistryType {

    public PreloadTexRegistration() {
        super(ForcePreloadTexture.class, "Texture");
        this.setLoadStage(LoadStage.POST_INIT);
    }

    @Override
    public boolean registerClass(AnnotationData data) throws Exception {
        Class cl = data.getTheClass();
        for(Field f : cl.getFields()) {
            //Raw Type
            if(f.getType() == ResourceLocation.class && Modifier.isStatic(f.getModifiers())) {
                ResourceLocation r = (ResourceLocation) f.get(null);
                if(isPNG(r)) { //Is a .png texture file, register it
                    preload(r);
                }
            }
            //Array
            if(f.getType() == ResourceLocation[].class && Modifier.isStatic(f.getModifiers())) {
                ResourceLocation[] ts = (ResourceLocation[]) f.get(null);
                if(ts != null) {
                    for(ResourceLocation r : ts) {
                        if(isPNG(r)) {
                            preload(r);
                        }
                    }
                }
            }
        }
        return true;
    }
    
    private boolean isPNG(ResourceLocation r) {
        return r != null && r.getResourcePath().contains(".png");
    }
    
    private void preload(ResourceLocation r) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(r);
    }

    @Override
    public boolean registerField(AnnotationData data) throws Exception {
        return false;
    }

    @Override
    public boolean registerMethod(AnnotationData data) throws Exception {
        return false;
    }

}
