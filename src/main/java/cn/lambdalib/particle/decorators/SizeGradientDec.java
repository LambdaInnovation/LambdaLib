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
package cn.lambdalib.particle.decorators;

import cn.lambdalib.particle.Particle;
import cn.lambdalib.util.entityx.MotionHandler;
import cn.lambdalib.util.generic.MathUtils;

/**
 * @author WeAthFolD
 */
public class SizeGradientDec implements ParticleDecorator {
	
	public float endScale = 0.7f;
	
	public SizeGradientDec(float es) {
		endScale = es;
	}

	@Override
	public void decorate(Particle particle) {
		particle.addMotionHandler(new MotionHandler<Particle>() {
			
			float startSize;

			@Override
			public String getID() {
				return "SizeGradientDecorator";
			}

			@Override
			public void onStart() {
				startSize = this.getTarget().size;
			}

			@Override
			public void onUpdate() {
				getTarget().size = startSize * 
					MathUtils.lerpf(1, endScale, ((float) getTarget().getParticleLife() / getTarget().getMaxLife()));
			}
			
		});
	}

}
