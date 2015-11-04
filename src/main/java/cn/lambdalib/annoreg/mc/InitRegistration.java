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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.lambdalib.annoreg.base.RegistrationClassOrField;
import cn.lambdalib.annoreg.core.LoadStage;
import cn.lambdalib.annoreg.core.RegModInformation;
import cn.lambdalib.annoreg.core.RegistryTypeDecl;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

@RegistryTypeDecl
public class InitRegistration extends RegistrationClassOrField<RegInit> {

	public InitRegistration() {
		super(RegInit.class, "SubmoduleInit");
		this.setLoadStage(LoadStage.INIT);
	}
	
	private boolean onSide(RegInit anno) {
		return FMLCommonHandler.instance().getSide().isClient() ||
				anno.side() != RegInit.Side.CLIENT_ONLY;
	}

	@Override
	protected void register(Class<?> value, RegInit anno) throws Exception {
		if (!onSide(anno))
			return;
		Method method = value.getDeclaredMethod("init");
		method.invoke(null);
	}

	@Override
	protected void register(Object value, RegInit anno, String field) throws Exception {
		if (!onSide(anno))
			return;
		Method method = value.getClass().getDeclaredMethod("init");
		method.invoke(value);
	}
}
