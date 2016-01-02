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
package cn.lambdalib.util.client.auxgui;

import net.minecraft.client.gui.ScaledResolution;
import cn.lambdalib.util.helper.GameTimer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Auxillary GUI interface class. This is a kind of GUI that doesn't make mouse gain focus. </br>
 * GUIs such as health indication, information indications are suitable of using this interface to define.
 * The class also provided a set of key-listening functions, based on LIKeyProcess. you can use event-based
 * methods to setup key listening.
 * @author WeathFolD
 */
@SideOnly(Side.CLIENT)
public abstract class AuxGui {
    
    // Intrusive states
    boolean lastFrameActive = false;
    long lastActivateTime;
    
    // Parameters
    /**
     * Whether this AuxGui needs fixed timestep update (ticking). If set to true tick() method will get called each tick.
     */
    protected boolean requireTicking = false;
    
    public AuxGui() {}
    
    protected long getTimeActive() {
        return GameTimer.getTime() - lastActivateTime;
    }
    
    private boolean disposed;
    
    public boolean isDisposed() {
        return disposed;
    }
    
    public void dispose() {
        disposed = true;
    }
    
    /**
     * Consistent GUI won't get removed when player is dead.
     */
    public boolean isConsistent() {
        return true;
    }
    
    /**
     * Called when this AuxGui instance is literally removed from the draw list.
     */
    public void onDisposed() {
        
    }
    
    /**
     * Called when this AuxGui instance is literally added into the draw list.
     */
    public void onAdded() {
        
    }
    
    /**
     * Judge if this GUI is a foreground GUI and interrupts key listening.
     */
    public abstract boolean isForeground();
    public abstract void draw(ScaledResolution sr);
    public void tick() {}
    
    public static void register(AuxGui gui) {
        AuxGuiHandler.register(gui);
    }
}
