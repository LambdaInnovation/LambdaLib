/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
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
