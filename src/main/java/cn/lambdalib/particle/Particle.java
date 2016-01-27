/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.particle;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEntity;
import cn.lambdalib.util.entityx.EntityAdvanced;
import cn.lambdalib.util.entityx.handlers.Rigidbody;
import cn.lambdalib.util.helper.Color;
import cn.lambdalib.util.helper.GameTimer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
@Registrant
@RegEntity(clientOnly = true)
@RegEntity.HasRender
public final class Particle extends EntityAdvanced implements ISpriteEntity {

    @RegEntity.Render
    public static RenderParticle render;

    public ResourceLocation texture = null;
    public Color color = Color.white();
    public float size = 1.0f;
    public boolean hasLight = false;
    public double gravity = 0.0;
    public boolean needRigidbody = true;
    /**
     * When set to true this particle is rotated with rotationYaw and
     * rotationPitch, else always faces the player.
     */
    public boolean customRotation = false;

    long creationTime;

    public int fadeInTime = 5;
    public int fadeTime;
    public int life = 10000000;
    double startAlpha;

    boolean updated;

    public Particle() {
        super(null);
    }

    public void fadeAfter(int life, int fadeTime) {
        this.life = life;
        this.fadeTime = fadeTime;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (ticksExisted > life) {
            int dt = ticksExisted - life;
            double alpha = 1 - (double) dt / fadeTime;
            if (alpha < 0) {
                setDead();
                alpha = 0;
            }
            color.a = alpha * startAlpha;
        } else if (ticksExisted < fadeInTime) {
            color.a = startAlpha * ((double) ticksExisted / fadeInTime);
        } else {
            color.a = startAlpha;
        }

        motionY -= gravity;
    }

    public void fromTemplate(Particle template) {
        this.texture = template.texture;
        this.color = template.color.copy();
        this.size = template.size;
        this.hasLight = template.hasLight;
        this.fadeTime = template.fadeTime;
        this.life = template.life;
        this.gravity = template.gravity;
        this.needRigidbody = template.needRigidbody;
        this.fadeInTime = template.fadeInTime;
        this.customRotation = template.customRotation;
        this.updated = false;
    }

    @Override
    protected void onFirstUpdate() {
        updated = true;
        if (needRigidbody)
            this.addMotionHandler(new Rigidbody());
        creationTime = GameTimer.getTime();
        startAlpha = color.a;
    }

    public long getParticleLife() {
        return GameTimer.getTime() - creationTime;
    }

    public long getMaxLife() {
        return life;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
    }

    @Override
    public void updateSprite(Sprite s) {
        s.cullFace = false;
        s.height = s.width = size;
        s.texture = texture;
        s.color = color;
        s.hasLight = hasLight;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @Override
    public boolean needViewOptimize() {
        return false;
    }

}
