/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
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
