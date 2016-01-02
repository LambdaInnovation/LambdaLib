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
