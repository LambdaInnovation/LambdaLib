/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.particle;

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.GL20;

import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.client.shader.ShaderSimple;
import cn.lambdalib.util.helper.Color;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * Represents a drawable sprite in origin. Always face (0, 0, -1).
 * 
 * @author WeAthFolD
 */
public final class Sprite {

    /**
     * If the texture is null draw pure-colored sprite.
     */
    public ResourceLocation texture;
    public float width = 1.0f, height = 1.0f;
    public Color color = Color.white();
    public boolean hasLight = false;
    public boolean cullFace = true;

    public Sprite() {
    }

    public Sprite(ResourceLocation rl) {
        texture = rl;
    }

    public Sprite setTexture(ResourceLocation rl) {
        texture = rl;
        return this;
    }

    public Sprite setSize(float w, float h) {
        width = w;
        height = h;
        return this;
    }

    public Sprite enableLight() {
        hasLight = true;
        return this;
    }

    public Sprite disableCullFace() {
        cullFace = false;
        return this;
    }

    public Sprite setColor(Color nc) {
        color = nc;
        return this;
    }

    public void draw() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (texture != null) {
            RenderUtils.loadTexture(texture);
        } else {
            glDisable(GL_TEXTURE_2D);
        }

        if (!cullFace) {
            glDisable(GL_CULL_FACE);
        }

        color.bind();
        Tessellator t = Tessellator.instance;
        float hw = width / 2, hh = height / 2;

        if (hasLight) {
            t.startDrawingQuads();
            t.setNormal(0, 0, -1);
            t.addVertexWithUV(-hw, hh, 0, 0, 0);
            t.addVertexWithUV(-hw, -hh, 0, 0, 1);
            t.addVertexWithUV(hw, -hh, 0, 1, 1);
            t.addVertexWithUV(hw, hh, 0, 1, 0);
            t.draw();
        } else {
            // Use legacy routine to avoid ShaderMod to ruin the render
            ShaderSimple.instance().useProgram();
            glBegin(GL_QUADS);
            glTexCoord2f(0, 0);
            glVertex3f(-hw, hh, 0);
            glTexCoord2f(0, 1);
            glVertex3f(-hw, -hh, 0);
            glTexCoord2f(1, 1);
            glVertex3f(hw, -hh, 0);
            glTexCoord2f(1, 0);
            glVertex3f(hw, hh, 0);
            glEnd();
            GL20.glUseProgram(0);
        }

        glEnable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
    }

}
