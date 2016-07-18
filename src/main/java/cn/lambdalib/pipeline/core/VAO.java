package cn.lambdalib.pipeline.core;

import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * RAII OpenGL Vertex Array Object.
 */
public class VAO {

    public static VAO create() {
        return new VAO(glGenVertexArrays());
    }

    private final int id;

    private VAO(int id) {
        this.id = id;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        glDeleteVertexArrays(id);
    }

    public int getID() {
        return id;
    }

}
