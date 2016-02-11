/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.gui.component;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.helper.Color;

/**
 * @author WeAthFolD
 */
public class Tint extends Component {
    
    public Color 
        idleColor,
        hoverColor;
    
    public boolean affectTexture = false;

    public double zLevel = 0.0;
    
    public static Tint get(Widget w) {
        return w.getComponent("Tint");
    }

    public Tint() {
        this(new Color(1, 1, 1, 0.6), new Color(1, 1, 1, 1));
    }

    public Tint(Color idle, Color hover, boolean _affectTexture) {
        this(idle, hover);
        affectTexture = _affectTexture;
    }
    
    public Tint(Color idle, Color hover) {
        super("Tint");

        idleColor = idle;
        hoverColor = hover;
        
        listen(FrameEvent.class, (w, event) -> {
            if(affectTexture) {
                DrawTexture dt = DrawTexture.get(w);
                if(dt != null) {
                    dt.color = event.hovering ? hoverColor : idleColor;
                }
            } else {
                if(event.hovering) hoverColor.bind();
                else idleColor.bind();
                
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                HudUtils.pushZLevel();
                HudUtils.zLevel = zLevel;
                HudUtils.colorRect(0, 0, w.transform.width, w.transform.height);
                HudUtils.popZLevel();
            }
        });
    }

    public Tint setAffectTexture() {
        affectTexture = true;
        return this;
    }
}
