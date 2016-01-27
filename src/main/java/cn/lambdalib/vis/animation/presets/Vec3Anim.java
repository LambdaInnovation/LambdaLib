/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.animation.presets;

import cn.lambdalib.vis.animation.Animation;
import cn.lambdalib.vis.curve.IFittedCurve;
import net.minecraft.util.Vec3;

/**
 * @author WeAthFolD
 */
public class Vec3Anim extends Animation {
    
    public Vec3 target;
    
    public IFittedCurve
        curveX,
        curveY,
        curveZ;
    
    public Vec3Anim() {}
    
    public Vec3Anim(Vec3 _target) {
        target = _target;
    }

    @Override
    public void perform(double timePoint) {
        if(curveX != null)
            target.xCoord = curveX.valueAt(timePoint);
        if(curveY != null)
            target.yCoord = curveY.valueAt(timePoint);
        if(curveZ != null)
            target.zCoord = curveZ.valueAt(timePoint);
    }

}
