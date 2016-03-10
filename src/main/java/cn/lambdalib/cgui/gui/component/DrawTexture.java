/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.gui.component;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL20.glUseProgram;

import cn.lambdalib.cgui.gui.Widget;
import cn.lambdalib.cgui.gui.event.FrameEvent;
import cn.lambdalib.util.client.HudUtils;
import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.helper.Color;
import net.minecraft.util.ResourceLocation;

/**
 * Draws a squared texture that fills the area of the given widget.
 */
public class DrawTexture extends Component {
    
    public static final ResourceLocation MISSING = new ResourceLocation("lambdalib:textures/cgui/missing.png");
    
    public ResourceLocation texture;
    
    public Color color;
    
    public double zLevel = 0;
    
    public boolean writeDepth = true;
    
    private int shaderId = 0;

    public DrawTexture() {
        this(MISSING);
    }

    public DrawTexture(ResourceLocation texture) {
        this(texture, Color.white());
    }

    public DrawTexture(String name, ResourceLocation _texture, Color _color) {
        super(name);
        this.texture = _texture;
        this.color = _color;

        listen(FrameEvent.class, (w, e) ->
        {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glDisable(GL_ALPHA_TEST);
            glDepthMask(writeDepth);
            glUseProgram(shaderId);
            color.bind();
            double preLevel = HudUtils.zLevel;
            HudUtils.zLevel = zLevel;

            if(texture != null && !texture.getResourcePath().equals("<null>")) {
                RenderUtils.loadTexture(texture);
                HudUtils.rect(0, 0, w.transform.width, w.transform.height);
            } else {
                HudUtils.colorRect(0, 0, w.transform.width, w.transform.height);
            }
            HudUtils.zLevel = preLevel;
            glUseProgram(0);
            glDepthMask(true);
        });
    }

    public DrawTexture(ResourceLocation _texture, Color _color) {
        this("DrawTexture", _texture, _color);
    }
    
    public void setShaderId(int id) {
        shaderId = id;
    }
    
    public DrawTexture setTex(ResourceLocation t) {
        texture = t;
        return this;
    }

    /**
     * Set the color as a **copy** of the given color.
     */
    public DrawTexture setColor(Color c) {
        this.color.from(c);
        return this;
    }
    
    public DrawTexture setColor4i(int r, int g, int b, int a) {
        color.setColor4d(r / 255.0, g / 255.0, b / 255.0, a / 255.0);
        return this;
    }
    
    public DrawTexture setColor4d(double _r, double _g, double _b, double _a) {
        color.setColor4d(_r, _g, _b, _a);
        return this;
    }
    
    public static DrawTexture get(Widget w) {
        return w.getComponent("DrawTexture");
    }

}
