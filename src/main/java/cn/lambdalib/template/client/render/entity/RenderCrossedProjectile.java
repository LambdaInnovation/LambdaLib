/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.template.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.helper.Motion3D;

/**
 * Render of 'projectile' type of renders. This renders a crossed square billboard at the entity position,
 * with the appropriate entity facing.
 */
public class RenderCrossedProjectile extends Render {
    
    public double 
        fpOffsetX = 0.5,
        fpOffsetY = -0.2,
        fpOffsetZ = -0.2;
    
    public double 
        tpOffsetX = 0.0,
        tpOffsetY = -0.2,
        tpOffsetZ = -0.4;

    protected double LENGTH;
    protected double HEIGHT;
    protected ResourceLocation TEXTURE_PATH;
    protected boolean renderTexture = true;
    protected float colorR, colorG, colorB;
    protected boolean ignoreLight = false;
    protected boolean playerViewOptm = true; //针对玩家的视角位置进行子弹位置微调
    
    public RenderCrossedProjectile(double l, double h, ResourceLocation texturePath) {
        LENGTH = l;
        HEIGHT = h;
        TEXTURE_PATH = texturePath;
    }
    
    public RenderCrossedProjectile(double l, double h, float a, float b, float c) {
        LENGTH = l;
        HEIGHT = h;
        setColor3f(a, b, c);
    }
    
    public RenderCrossedProjectile setColor3f(float a, float b, float c) {
        renderTexture = false;
        colorR = a;
        colorG = b;
        colorB = c;
        return this;
    }
    
    public RenderCrossedProjectile setViewOptimize(boolean b) {
        playerViewOptm = b;
        return this;
    }
    
    public RenderCrossedProjectile setIgnoreLight(boolean b) {
        ignoreLight = b;
        return this;
    }

    @Override
    public void doRender(Entity entity, double par2, double par4,
            double par6, float par8, float par9) {
        Motion3D motion = new Motion3D(entity);
        Tessellator t = Tessellator.instance;

        GL11.glPushMatrix(); {
            Vec3d v1 = newV3(0, HEIGHT, 0),
                    v2 = newV3(0, -HEIGHT, 0), 
                    v3 = newV3(LENGTH, -HEIGHT, 0),
                    v4 = newV3(LENGTH, HEIGHT, 0),
                    v5 = newV3(0, 0, -HEIGHT), 
                    v6 = newV3(0, 0, HEIGHT), 
                    v7 = newV3(LENGTH, 0, HEIGHT), 
                    v8 = newV3(LENGTH, 0, -HEIGHT);

            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            if(renderTexture) {
                bindTexture(TEXTURE_PATH);
            } else {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glColor3f(colorR, colorG, colorB);
            }
            if(ignoreLight) {
                GL11.glDisable(GL11.GL_LIGHTING);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            }
            
            GL11.glTranslatef((float) par2, (float) par4, (float) par6);
            
            GL11.glRotatef(90 + entity.rotationYaw, 0.0F, -1.0F, 0.0F); // 左右旋转
            GL11.glRotatef(-entity.rotationPitch, 0.0F, 0.0F, 1.0F); // 上下旋转
            
            if(this.playerViewOptm) {
                boolean firstPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView == 0;
                if(firstPerson) {
                    GL11.glTranslated(fpOffsetX, fpOffsetY, fpOffsetZ);
                } else {
                    GL11.glTranslated(tpOffsetX, tpOffsetY, tpOffsetZ);
                }
            }
            
            t.startDrawingQuads();
            if(ignoreLight) 
                t.setBrightness(15728880);
            
            RenderUtils.addVertex(v1, 0, 0);
            RenderUtils.addVertex(v2, 0, 1);
            RenderUtils.addVertex(v3, 1, 1);
            RenderUtils.addVertex(v4, 1, 0);
            
            RenderUtils.addVertex(v5, 0, 0);
            RenderUtils.addVertex(v6, 0, 1);
            RenderUtils.addVertex(v7, 1, 1);
            RenderUtils.addVertex(v8, 1, 0);
            
            t.draw();
            
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_BLEND);
        } GL11.glPopMatrix();
    }

    public static Vec3d newV3(double x, double y, double z) {
        return Vec3.createVectorHelper(x, y, z);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE_PATH;
    }
}
