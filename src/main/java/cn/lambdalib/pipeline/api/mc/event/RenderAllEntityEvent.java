package cn.lambdalib.pipeline.api.mc.event;

import cn.lambdalib.pipeline.api.mc.EntityRenderUtils;
import cpw.mods.fml.common.eventhandler.Event;
import org.lwjgl.util.vector.Matrix4f;

/**
 * Fired at the beginning of an entity rendering pass.
 */
public class RenderAllEntityEvent extends Event {

    public final int pass;
    public final float partialTicks;
    public final float deltaTime;

    public RenderAllEntityEvent(int pass, float partialTicks, float deltaTime) {
        this.pass = pass;
        this.partialTicks = partialTicks;
        this.deltaTime = deltaTime;
    }

    public Matrix4f playerViewMatrix() {
        return EntityRenderUtils.getPlayerViewMatrix();
    }

    public Matrix4f projectionMatrix() {
        return EntityRenderUtils.getProjectionMatrix();
    }

    public Matrix4f pvpMatrix() {
        return EntityRenderUtils.getPVPMatrix();
    }

}
