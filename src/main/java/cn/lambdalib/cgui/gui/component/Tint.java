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
}
