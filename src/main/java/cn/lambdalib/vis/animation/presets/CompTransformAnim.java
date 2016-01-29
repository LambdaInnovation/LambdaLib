/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
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
