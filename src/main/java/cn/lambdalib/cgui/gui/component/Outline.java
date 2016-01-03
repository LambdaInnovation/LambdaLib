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

import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.helper.Color;

/**
 * @author WeAthFolD
 */
public class Outline extends Component {
    
    public Color color;
    public float lineWidth = 2;

    public Outline() {
        this(Color.white());
    }

    public Outline(Color _color) {
        super("Outline");

        color = _color;
        
        listen(FrameEvent.class, (w, e) -> {
            color.bind();
            HudUtils.drawRectOutline(0, 0, w.transform.width, w.transform.height, lineWidth);
            GL11.glColor4f(1, 1, 1, 1);
        });
    }

}
