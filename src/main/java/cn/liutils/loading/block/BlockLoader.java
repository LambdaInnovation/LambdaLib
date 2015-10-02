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
package cn.liutils.loading.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import cn.liutils.core.LIUtils;
import cn.liutils.loading.Loader;
import cn.liutils.render.material.Material;

/**
 * @author WeAthFolD
 *
 */
public class BlockLoader extends Loader<Block> {
	
	//TODO: Do we need to filter rules?
	private List<BlockLoadRule> rules = new ArrayList();
	
	boolean pass1;
	
	public BlockLoader() {
		//Add default rules
		
		addRule(new DefaultRules.Hardness());
		addRule(new DefaultRules.HarvestLevel());
		addRule(new DefaultRules.LightLevel());
		addRule(new DefaultRules.StepSound());
		addRule(new DefaultRules.Texture());
		addRule(new DefaultRules.UnlocalizedName());
	}
	
	protected void addRule(BlockLoadRule rule) {
		rules.add(rule);
	}

	@Override
	protected Block load(String name, Loader<Block>.ObjectNamespace ns) {
		pass1 = true;
		try {
			String clazzName = ns.getString("blockType");
			
			String matName = ns.getString("material");
			//TODO: Internal mapping!
			Material mat = (Material) Material.class.getField(matName).get(null);
			
			Class<? extends Block> clazz = (Class<? extends Block>) Class.forName(clazzName);
			
			Block block = clazz.getConstructor(Material.class).newInstance(mat);
			doInit(block, ns, name);
			
			return block;
		} catch(Exception e) {
			LIUtils.log.fatal("An error occured doing block construction.");
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected void finishedLoading(String name, Block object, Loader<Block>.ObjectNamespace ns) {
		pass1 = false;
		doInit(object, ns, name);
	}
	
	private void doInit(Block block, Loader<Block>.ObjectNamespace ns, String name) {
		try {
			checkRuleset(block, ns, rules, name);
			if(block instanceof BlockLoadRuleProvider) {
				checkRuleset(block, ns, ((BlockLoadRuleProvider)block).getLoadRules(), name);
			}
		} catch(Exception e) {
			LIUtils.log.fatal("An error occured applying loader rules.");
			e.printStackTrace();
		}
	}
	
	private void checkRuleset(Block block, Loader<Block>.ObjectNamespace ns,
		Iterable<BlockLoadRule> iterable, String name) throws Exception {
		for(BlockLoadRule rule : iterable) {
			if(pass1) {
				rule.load(block, ns, name);
			} else {
				rule.finishedLoad(block, ns, name);
			}
		}
	}

}
