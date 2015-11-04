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
package cn.lambdalib.vis.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author WeAthFolD
 */
public class AnimationList extends Animation {
	
	private List<Animation> anims = new ArrayList();	
	
	public AnimationList(Animation ..._anims) {
		for(Animation a : _anims)
			anims.add(a);
	}
	
	public AnimationList(Collection<Animation> _anims) {
		anims.addAll(_anims);
	}

	@Override
	public void perform(double timePoint) {
		for(Animation a : anims)
			a.perform(timePoint);
	}
	
}
