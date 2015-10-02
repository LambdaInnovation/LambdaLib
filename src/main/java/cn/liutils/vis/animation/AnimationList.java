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
package cn.liutils.vis.animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author WeAthFolD
 */
public class AnimationList implements IAnimation {
	
	private List<IAnimation> anims = new ArrayList();	
	
	public AnimationList(IAnimation ..._anims) {
		for(IAnimation a : _anims)
			anims.add(a);
	}
	
	public AnimationList(Collection<IAnimation> _anims) {
		anims.addAll(_anims);
	}

	@Override
	public void perform(long timePoint) {
		for(IAnimation a : anims)
			a.perform(timePoint);
	}
	
}
