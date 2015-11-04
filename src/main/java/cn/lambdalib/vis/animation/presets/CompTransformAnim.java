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
package cn.lambdalib.vis.animation.presets;

import cn.lambdalib.vis.animation.Animation;
import cn.lambdalib.vis.curve.IFittedCurve;
import cn.lambdalib.vis.model.CompTransform;

/**
 * @author WeAthFolD
 */
public class CompTransformAnim extends Animation {
	
	public CompTransform target;
	
	public Vec3Anim
		animTransform = new Vec3Anim(),
		animPivot = new Vec3Anim(),
		animRotation = new Vec3Anim();
	
	public IFittedCurve
		curveScale;
	
	public CompTransformAnim(CompTransform _transform) {
		target = _transform;
	}
	
	public CompTransformAnim() {}

	@Override
	public void perform(double timePoint) {
		animTransform.target = target.transform;
		animPivot.target = target.pivotPt;
		animRotation.target = target.rotation;
		
		animTransform.perform(timePoint);
		animPivot.perform(timePoint);
		animRotation.perform(timePoint);
		
		if(curveScale != null)
			target.scale = curveScale.valueAt(timePoint);
	}

}
