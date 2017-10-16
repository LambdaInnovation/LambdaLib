/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.version;

import cn.lambdalib.annoreg.base.RegistrationClassSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import net.minecraftforge.fml.common.Mod;

@RegistryTypeDecl
public class VersionCheckerRegister extends RegistrationClassSimple<VersionUpdateUrl,Object>
{

	public VersionCheckerRegister() {
		super(VersionUpdateUrl.class, "VersionUpdateUrl");
		this.setLoadStage(LoadStage.INIT);
	}
	
	@Override
    protected void register(Class<? extends Object> theClass, VersionUpdateUrl anno) throws Exception 
	{
		Mod mod=theClass.getAnnotationsByType(Mod.class)[0];//WARN
		String localVersion=mod.version();
		String modid=mod.modid();
		String modname=mod.name();
		String repourl=anno.repoUrl();
		String release_api="https://api.github.com/repos/"+repourl.substring(repourl.indexOf("github.com/")+11)+"/releases";
		CheckManger.instance().addMod(modid,new String[]{modname,localVersion,release_api});
    }
}
