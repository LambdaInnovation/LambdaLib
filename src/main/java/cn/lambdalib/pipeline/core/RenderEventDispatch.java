package cn.lambdalib.pipeline.core;

import cn.lambdalib.pipeline.api.GraphicPipeline;
import cn.lambdalib.pipeline.api.mc.event.RenderAllEntityEvent;
import cn.lambdalib.util.helper.GameTimer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Internal class don't touch or use!
 */
public class RenderEventDispatch {

    private static boolean firstFrame = true;
    private static long lastFrameTime = -1;
    private static float thisFrameDeltaTime = 0;

    private static final Vector4f UP_VEC4 = new Vector4f(0, 1, 0, 1);

    private static final Vector3f
        cameraUp = new Vector3f(),
        cameraPos = new Vector3f();

    private static final Matrix4f tmpResult = new Matrix4f();

    private static final FloatBuffer matrixAcqBuffer = BufferUtils.createFloatBuffer(16);

    public static final Matrix4f
        projMatrix = new Matrix4f(),
        playerViewMatrix = new Matrix4f(),
        pvpMatrix = new Matrix4f();

    public static final GraphicPipeline entityPipeline = new GraphicPipeline();

    // Following method signatures used by ASM

    public static void beginRenderEntities(float partialTicks) {
        int pass = MinecraftForgeClient.getRenderPass();
        long time = GameTimer.getTime();

        float deltaTime;

        if (pass == 0) {
            // Update matrices
            acquireMatrix(GL_MODELVIEW_MATRIX, playerViewMatrix);
            acquireMatrix(GL_PROJECTION_MATRIX, projMatrix);
            Matrix4f.mul(projMatrix, playerViewMatrix, pvpMatrix);

            // Find camera up vector and camera position
            {
                Matrix4f inv = tmpResult;
                Matrix4f.invert(playerViewMatrix, inv);

                cameraPos.x = inv.m30;
                cameraPos.y = inv.m31;
                cameraPos.z = inv.m32;

                Vector4f tmp = Matrix4f.transform(inv, UP_VEC4, null);
                Vector3f p = new Vector3f(tmp.x, tmp.y, tmp.z);
                Vector3f.sub(p, cameraPos, cameraUp);
                cameraUp.normalise();
            }

            // Update delta time
            if (firstFrame) {
                firstFrame = false;
                deltaTime = 0;
            } else {
                deltaTime = (time - lastFrameTime) / 1000.0f;
            }

            lastFrameTime = time;
            thisFrameDeltaTime = deltaTime;
        } else {
            deltaTime = thisFrameDeltaTime;
        }

        MinecraftForge.EVENT_BUS.post(new RenderAllEntityEvent(pass, partialTicks, deltaTime));
    }

    public static void endRenderEntities(float partialTicks) {
        entityPipeline.flush();

        // Restore GL states
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

    }

    //

    public static Vector3f getCameraUp() {
        return cameraUp;
    }

    public static Vector3f getCameraPos() {
        return cameraPos;
    }


    //

    private static void acquireMatrix(int matrixType, Matrix4f dst) {
        matrixAcqBuffer.clear();
        glGetFloat(matrixType, matrixAcqBuffer);
        dst.load(matrixAcqBuffer);
    }

}
