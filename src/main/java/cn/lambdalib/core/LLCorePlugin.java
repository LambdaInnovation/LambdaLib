/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.core;

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

public class LLCorePlugin implements IFMLLoadingPlugin {

    private static boolean deobfEnabled;

    public static boolean isDeobfEnabled() {
        return deobfEnabled;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "cn.lambdalib.pipeline.core.PipelineTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return "cn.lambdalib.core.LLModContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        deobfEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
