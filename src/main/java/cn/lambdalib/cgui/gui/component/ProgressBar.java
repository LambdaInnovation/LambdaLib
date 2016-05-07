/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.gui.component;

import java.util.Random;

import cn.lambdalib.util.generic.MathUtils;
import org.lwjgl.opengl.GL11;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.helper.Color;
import cn.lambdalib.util.helper.GameTimer;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 *
 */
public class ProgressBar extends Component {
    
    public enum Direction { RIGHT, LEFT, UP, DOWN };
    
    public boolean illustrating = false;
    public ResourceLocation texture;
    public Direction dir = Direction.RIGHT;
    public double progress;
    public Color color = Color.white();

    public ProgressBar() {
        super("ProgressBar");
        listen(FrameEvent.class, (wi, e) -> {
            if(illustrating) {
                progress = 0.5 * (1 + Math.sin(GameTimer.getAbsTime() / 1000.0));
            }
            
            {
                double disp = MathUtils.clampd(0, 1, progress);

                double x, y, u = 0, v = 0, w, h, tw, th;
                double width = wi.transform.width, height = wi.transform.height;
                switch(dir) {
                case RIGHT:
                    w = width * disp;
                    h = height;
                    x = y = 0;
                    
                    u = 0;
                    v = 0;
                    tw = disp;
                    th = 1;
                    break;
                case LEFT:
                    w = width * disp;
                    h = height;
                    x = width - w;
                    y = 0;
                    
                    u = (1 - disp);
                    v = 0;
                    tw = disp;
                    th = 1;
                    break;
                case UP:
                    w = width;
                    h = height * disp;
                    x = 0;
                    y = height * (1 - disp);
                    
                    u = 0;
                    v = (1 - disp);
                    tw = 1;
                    th = disp;
                    break;
                case DOWN:
                    w = width;
                    h = height * disp;
                    x = y = 0;
                    u = 0;
                    v = 0;
                    tw = 1;
                    th = disp;
                    break;
                default:
                    throw new RuntimeException("niconiconi, WTF??");
                }
                if(texture != null && !texture.getResourcePath().equals("<null>")) {
                    RenderUtils.loadTexture(texture);
                } else {
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                }
                color.bind();
                HudUtils.rawRect(x, y, u, v, w, h, tw, th);
                GL11.glEnable(GL11.GL_TEXTURE_2D);
            }
        });
    }
    
    public ProgressBar setDirection(Direction dir) {
        this.dir = dir;
        return this;
    }
    
    public static ProgressBar get(Widget w) {
        return w.getComponent("ProgressBar");
    }

    
}
