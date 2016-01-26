package cn.lambdalib.util.version;

import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.SideOnly;
import cn.lambdalib.annoreg.base.RegistrationClassSimple;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cn.lambdalib.annoreg.mc.RegTileEntity;

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
		String release_api="api.github.com/repos/"+repourl.substring(repourl.indexOf("github.com/")+11)+"/releases";
		CheckManger.instance().addMod(modid,new String[]{modname,localVersion,release_api});
    }
}
