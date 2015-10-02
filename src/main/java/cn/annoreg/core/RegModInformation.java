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
package cn.annoreg.core;

import cn.annoreg.ARModContainer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;

public class RegModInformation {
	
	private String modid;
	private String pkg, prefix, res;
	/**
	 * Cached mod instance.
	 */
	private Object mod;
	
	/**
	 * Store class name before loading the mod class.
	 */
	private String modClassName;
	
	private void loadModClass() {
	    if (pkg == null) {
            Class<?> modClass;
	        try {
	            modClass = Class.forName(modClassName);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException("Can not get mod class.");
            }

	        if (!modClass.isAnnotationPresent(RegistrationMod.class)) {
	            //This should not happen.
	            ARModContainer.log.error("Unable to create RegistryMod {}", modClass.getCanonicalName());
	        }
	        
	        //Get mod information.
	        RegistrationMod rm = modClass.getAnnotation(RegistrationMod.class);
	        this.pkg = rm.pkg();
	        this.prefix = rm.prefix();
	        this.res = rm.res();
	        modid = modClass.getAnnotation(Mod.class).modid();
	    }
	}
	
	public RegModInformation(String className) {
	    modClassName = className;
	}

	public String getPackage() {
	    loadModClass();
		return pkg;
	}
	
	public String getPrefix() {
        loadModClass();
		return prefix;
	}
	
	public String getRes(String id) {
        loadModClass();
		return res + ":" + id;
	}
	
	public Object getModInstance() {
        loadModClass();
		if (mod != null) return mod;
		ModContainer mc = Loader.instance().getIndexedModList().get(modid);
		if (mc != null) {
			mod = mc.getMod();
			return mod;
		} else {
			return null;
		}
	}
	
	public String getModID() {
        loadModClass();
		return modid;
	}
}
