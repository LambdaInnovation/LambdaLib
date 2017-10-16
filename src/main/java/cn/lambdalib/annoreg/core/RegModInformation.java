/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.annoreg.core;

import cn.lambdalib.core.LLModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;

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
                LLModContainer.log.error("Unable to create RegistryMod {}", modClass.getCanonicalName());
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
