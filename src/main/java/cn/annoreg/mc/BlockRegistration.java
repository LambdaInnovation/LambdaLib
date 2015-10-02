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

import cn.annoreg.base.RegistrationFieldSimple;
import cn.annoreg.core.LoadStage;
import cn.annoreg.core.RegModInformation;
import cn.annoreg.core.RegistryTypeDecl;
import net.minecraft.block.Block;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.registry.GameRegistry;

@RegistryTypeDecl
public class BlockRegistration extends RegistrationFieldSimple<RegBlock, Block> {

	public BlockRegistration() {
		super(RegBlock.class, "Block");
		this.setLoadStage(LoadStage.PRE_INIT);
		
		this.addWork(RegBlock.OreDict.class, new PostWork<RegBlock.OreDict, Block>() {
			@Override
			public void invoke(RegBlock.OreDict anno, Block obj) throws Exception {
				OreDictionary.registerOre(anno.value(), obj);
			}
		});
		
		this.addWork(RegBlock.BTName.class, new PostWork<RegBlock.BTName, Block>() {
			@Override
			public void invoke(RegBlock.BTName anno, Block obj) throws Exception {
				obj.setBlockName(getCurrentMod().getPrefix() + anno.value());
				obj.setBlockTextureName(getCurrentMod().getRes(anno.value()));
			}
		});
		
	}

	@Override
	protected void register(Block value, RegBlock anno, String field) throws Exception {
		GameRegistry.registerBlock(value, anno.item(), getSuggestedName());
	}

}
