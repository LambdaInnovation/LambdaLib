/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.animation.presets;

import cn.lambdalib.util.generic.VecUtils;
import cn.lambdalib.vis.animation.Animation;
import cn.lambdalib.vis.curve.IFittedCurve;
import net.minecraft.util.math.Vec3d;

/**
 * @author WeAthFolD
 */
public class Vec3Anim extends Animation {
    
    public Vec3d target;
    
    public IFittedCurve
        curveX,
        curveY,
        curveZ;
    
    public Vec3Anim() {}
    
    public Vec3Anim(Vec3d _target) {
        target = _target;
    }

    @Override
    public void perform(double timePoint) {
        double x=target.x,y=target.y,z=target.z;
        if(curveX != null)
            x = curveX.valueAt(timePoint);
        if(curveY != null)
            y = curveY.valueAt(timePoint);
        if(curveZ != null)
            z = curveZ.valueAt(timePoint);
        target = VecUtils.vec(x,y,z);
    }

}
