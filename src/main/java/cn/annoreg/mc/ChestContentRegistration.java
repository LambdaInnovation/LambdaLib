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
package cn.annoreg.mc;

import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import cn.annoreg.base.RegistrationFieldSimple;
import cn.annoreg.core.LoadStage;
import cn.annoreg.core.RegistryTypeDecl;
import cn.annoreg.core.RegistrationWithPostWork.PostWork;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author KSkun
 */
@RegistryTypeDecl
public class ChestContentRegistration extends RegistrationFieldSimple<RegChestContent, WeightedRandomChestContent> {
	
	public ChestContentRegistration() {
		super(RegChestContent.class, "ChestContent");
		this.setLoadStage(LoadStage.INIT);
	}

	@Override
	protected void register(WeightedRandomChestContent value, RegChestContent anno, String field) 
			throws Exception {
		for(int i : anno.value()) {
			ChestGenHooks.addItem(type(i), value);
		}
	}
	
	private String type(int type) {
		switch(type) {
		case 0:
			return "dungeonChest";
		case 1:
			return "villageBlacksmith";
		case 2:
			return "pyramidDesertyChest";
		case 3:
			return "pyramidJungleChest";
		case 4:
			return "mineshaftCorridor";
		case 5:
			return "pyramidJungleDispenser";
		case 6:
			return "strongholdCorridor";
		case 7:
			return "strongholdLibrary";
		case 8:
			return "strongholdCrossing";
		case 9:
			return "bonusChest";
		}
		return null;
	}

}
