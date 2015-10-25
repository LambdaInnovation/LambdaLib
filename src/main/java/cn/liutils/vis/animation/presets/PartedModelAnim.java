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
package cn.liutils.vis.animation.presets;

import java.util.HashMap;
import java.util.Map;

import cn.liutils.vis.animation.Animation;
import cn.liutils.vis.model.PartedModel;

/**
 * @author WeAthFolD
 */
public class PartedModelAnim extends Animation {
	
	public PartedModel target;
	
	private Map<String, CompTransformAnim> partAnims = new HashMap();
	
	CompTransformAnim directAnim;
	
	public PartedModelAnim(PartedModel model) {
		target = model;
	}
	
	public PartedModelAnim() {}
	
	public void init(CompTransformAnim anim) {
		directAnim = anim;
		anim.target = target.transform;
	}
	
	/**
	 * Init animation of the given part name with the given animation.
	 * The part must be of type ModelPart, otherwise an exception is thrown.
	 */
	public void init(String partName, CompTransformAnim anim) {
		PartedModel part = target.getChild(partName);
		if(part != null) {
			partAnims.put(partName, anim);
			anim.target = part.transform;
		} else 
			throw new RuntimeException("Part " + partName + " invalid");
	}
	
	public CompTransformAnim animOf(String partName) {
		return partAnims.get(partName);
	}

	@Override
	public void perform(double timePoint) {
		for(CompTransformAnim anim : partAnims.values())
			anim.perform(timePoint);
		
		if(directAnim != null)
			directAnim.perform(timePoint);
	}

}
