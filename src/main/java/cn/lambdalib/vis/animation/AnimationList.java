/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
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
