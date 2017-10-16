/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.particle;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.util.client.RenderUtils;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * A delegator renderer for Sprite and Entities that implements ISpriteEntity.
 * 
 * @author WeAthFolD
 */
public class RenderParticle extends Render {

    static Sprite sprite = new Sprite();

    public RenderParticle() {
        this.shadowOpaque = 0;
    }

    @Override
    public void doRender(Entity ent, double x, double y, double z, float a, float b) {
        if (RenderUtils.isInShadowPass())
            return;

        Particle ish = (Particle) ent;
        if (!ish.updated)
            return;

        ish.updateSprite(sprite);

        GL11.glAlphaFunc(GL11.GL_GREATER, 0.05f);
        GL11.glPushMatrix();

        if (ish.needViewOptimize()) {
            GL11.glTranslated(0, -0.2, 0);
        }

        GL11.glTranslated(x, y, z);
        if (ish.customRotation) {
            GL11.glRotatef(ish.rotationYaw, 0, 1, 0);
            GL11.glRotatef(ish.rotationPitch, 0, 0, 1);
        } else {
            GL11.glRotatef(180F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        }

        sprite.draw();

        GL11.glPopMatrix();
        GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.1f);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return null;
    }

}
