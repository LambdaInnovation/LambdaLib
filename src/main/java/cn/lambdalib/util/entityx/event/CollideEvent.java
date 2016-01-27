/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.entityx.event;

import cn.lambdalib.util.entityx.EntityEvent;
import cn.lambdalib.util.entityx.EntityEventHandler;
import net.minecraft.util.MovingObjectPosition;

/**
 * @author WeAthFolD
 */
public class CollideEvent extends EntityEvent {

    public final MovingObjectPosition result;
    
    public CollideEvent(MovingObjectPosition mop) {
        result = mop;
    }
    
    public static abstract class CollideHandler extends EntityEventHandler<CollideEvent> {

        @Override
        public Class<? extends EntityEvent> getHandledEvent() {
            return CollideEvent.class;
        }
        
    }
    
}
