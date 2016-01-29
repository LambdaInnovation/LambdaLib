/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.entityx.handlers;

import cn.lambdalib.util.entityx.MotionHandler;

public class Life extends MotionHandler {
    
    public int ticks;

    public Life(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public String getID() {
        return "Life";
    }

    @Override
    public void onStart() {}

    @Override
    public void onUpdate() {
        if(getTarget().ticksExisted == ticks) {
            getTarget().setDead();
        }
    }

}
