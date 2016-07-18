package cn.lambdalib.pipeline.core;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

/**
 * RAII OpenGL Buffer Object.
 */
public class GLBuffer {

    public static GLBuffer create() {
        return new GLBuffer(glGenBuffers());
    }

    private final int bufferID;

    private GLBuffer(int bufferID) {
        this.bufferID = bufferID;
    }

    public int getID() {
        return bufferID;
    }

    @Override
    protected void finalize() throws Throwable {
        glDeleteBuffers(bufferID);
        super.finalize();
    }
}
