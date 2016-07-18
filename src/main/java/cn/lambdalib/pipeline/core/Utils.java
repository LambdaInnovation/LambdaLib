package cn.lambdalib.pipeline.core;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.opengl.GL11.*;

/**
 * Common unordered utils.
 */
public class Utils {

    public static final Logger log = LogManager.getLogger("Pipeline");

    public static RuntimeException notImplemented() {
        throw new RuntimeException("Method not implemented");
    }

    public static InputStream getResourceStream(ResourceLocation res) {
        try {
            String domain = res.getResourceDomain(), path = res.getResourcePath();
            return Preconditions.checkNotNull(Utils.class.getResourceAsStream("/assets/" + domain + "/" + path), "Invalid resource " + res);
        } catch(Exception e) {
            throw new RuntimeException("Invalid resource " + res, e);
        }
    }

    public static String toString(ResourceLocation res) {
        try {
            return IOUtils.toString(getResourceStream(res));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static String getLastGLError() {
        int error = glGetError();

        switch (error) {
            case GL_INVALID_ENUM:
                return "InvalidEnum";
            case GL_INVALID_VALUE:
                return "InvalidValue";
            case GL_INVALID_OPERATION:
                return "InvalidOperation";
            case GL_STACK_OVERFLOW:
                return "StackOverflow";
            case GL_STACK_UNDERFLOW:
                return "StackUnderflow";
            case GL_OUT_OF_MEMORY:
                return "OutOfMemory";
            case GL_NO_ERROR:
                return "None";
            default:
                return "Unknown";
        }
    }

    private Utils() {}

}
