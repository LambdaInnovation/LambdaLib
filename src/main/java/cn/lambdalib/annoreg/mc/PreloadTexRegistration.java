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

}
