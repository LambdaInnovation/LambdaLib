package cn.annoreg.mc;

import java.lang.reflect.Method;

import cn.annoreg.base.RegistrationClassOrField;
import cn.annoreg.core.LoadStage;
import cn.annoreg.core.RegistryTypeDecl;
import cpw.mods.fml.common.FMLCommonHandler;

@RegistryTypeDecl
public class PostInitRegistration extends RegistrationClassOrField<RegPostInit> {

	public PostInitRegistration() {
		super(RegPostInit.class, "RegPostInit");
		this.setLoadStage(LoadStage.POST_INIT);
	}
	
	private boolean onSide(RegPostInit anno) {
		return FMLCommonHandler.instance().getSide().isClient() ||
				anno.side() != RegInit.Side.CLIENT_ONLY;
	}

	@Override
	protected void register(Class<?> value, RegPostInit anno) throws Exception {
		if (!onSide(anno))
			return;
		Method method = value.getDeclaredMethod("postInit");
		method.invoke(null);
	}

	@Override
	protected void register(Object value, RegPostInit anno, String field) throws Exception {
		if (!onSide(anno))
			return;
		Method method = value.getClass().getDeclaredMethod("postInit");
		method.invoke(value);
	}
}

