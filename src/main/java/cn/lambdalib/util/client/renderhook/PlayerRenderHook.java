/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client.renderhook;

import cn.lambdalib.util.helper.GameTimer;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Register through DummyRenderData.get(player).addRenderHook(hook)
 * @author WeAthFolD
 */
public abstract class PlayerRenderHook {
    
    EntityPlayer player;
    boolean disposed;
    long createTime = GameTimer.getTime();
    
    public void renderHand(boolean firstPerson) {}
    
    public void dispose() {
        disposed = true;
    }
    
    public final EntityPlayer getPlayer() {
        return player;
    }
    
    protected long getDeltaTime() {
        return GameTimer.getTime() - createTime;
    }
    
}
