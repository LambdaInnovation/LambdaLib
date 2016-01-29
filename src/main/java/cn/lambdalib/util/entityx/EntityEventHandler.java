/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.entityx;

/**
 * @author WeAthFolD
 */
public abstract class EntityEventHandler<T extends EntityEvent> {
    
    public boolean active = true;

    public abstract Class<? extends EntityEvent> getHandledEvent();
    
    public abstract void onEvent(T event);
}
