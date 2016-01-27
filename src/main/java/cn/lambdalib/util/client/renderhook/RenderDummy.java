/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client.renderhook;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glTranslated;

import cn.lambdalib.util.deprecated.ViewOptimize;
import cn.lambdalib.util.generic.MathUtils;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * @author WeAthFolD
 */
public class RenderDummy extends Render {

    @Override
    public void doRender(Entity _entity, double x, double y, double z, float a, float b) {
        EntityDummy entity = (EntityDummy) _entity;
        glPushMatrix();
        glTranslated(x, y, z);
        
        boolean fp = ViewOptimize.isFirstPerson(entity);
        
        float yy, ly;
        if(fp) {
            yy = entity.rotationYawHead;
            ly = entity.lastRotationYawHead;
        } else {
            yy = entity.rotationYaw;
            ly = entity.lastRotationYaw;
        }
        
        float yaw = MathUtils.lerpf(ly, yy, b);
        glRotated(180 - yaw, 0, 1, 0);
        
        // Render hand
        
        if(fp) {
            glRotated(-entity.rotationPitch, 1, 0, 0);
        } else {
            ViewOptimize.fixThirdPerson();
        }
        
        for(PlayerRenderHook hook : entity.data.renderers) {
            hook.renderHand(fp);
        }
        glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity e) {
        return null;
    }

}
