/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * 
 * @author Violet
 *
 */
public abstract class LIHandler<T extends Event> {
    
    private boolean dead = false;
    
    public final boolean isDead() {
        return dead;
    }
    
    public final void setDead() {
        dead = true;
    }
    
    public final void setAlive() {
        dead = false;
    }

    public final void trigger(T event) {
        if (!onEvent(event))
            throw new RuntimeException("Unexpected event(" + event.getClass().getName() + ") for " + this.getClass().getName());
    }
    
    /**
     * Return false if some error occured.
     * SUGGESTION: return false if this handler should be set dead?
     */
    protected abstract boolean onEvent(T event);
}
