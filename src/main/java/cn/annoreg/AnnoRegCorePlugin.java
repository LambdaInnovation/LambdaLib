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
package cn.annoreg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import cpw.mods.fml.common.MetadataCollection;
import cpw.mods.fml.relauncher.IFMLCallHook;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class AnnoRegCorePlugin implements IFMLLoadingPlugin {
    public static MetadataCollection mc = MetadataCollection.from(null,"");
	
	@Override
	public String[] getASMTransformerClass() {
		return new String[] {"cn.annoreg.asm.RegistryTransformer"};
	}

	@Override
	public String getModContainerClass() {
		return "cn.annoreg.ARModContainer";
	}

	@Override
	public String getSetupClass() {
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data) {
	    File modFile = (File) data.get("coremodLocation");
	    if (modFile.isDirectory()) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(new File(modFile, "mcmod.info"));
                mc = MetadataCollection.from(fis, modFile.getName());
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
	    } else {
	        JarFile jar = null;
            try {
                jar = new JarFile(modFile);
                ZipEntry modInfo = jar.getEntry("mcmod.info");
                mc = MetadataCollection.from(jar.getInputStream(modInfo), modFile.getName());
                jar.close();
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (jar != null) {
                    try {
                        jar.close();
                    } catch (IOException e) {
                    }
                }
            }
	    }
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
