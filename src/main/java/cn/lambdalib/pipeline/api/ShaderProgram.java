package cn.lambdalib.pipeline.api;

import cn.lambdalib.pipeline.api.ProgramCreationException.ErrorType;
import cn.lambdalib.pipeline.core.Utils;
import com.google.common.base.Throwables;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    public static ShaderProgram load(String vertexShader, String fragmentShader) throws ProgramCreationException {
        int program = glCreateProgram();

        linkShader(program, GL_VERTEX_SHADER, vertexShader);
        linkShader(program, GL_FRAGMENT_SHADER, fragmentShader);

        glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            String log = glGetProgramInfoLog(program, glGetProgrami(program, GL_INFO_LOG_LENGTH));
            throw new ProgramCreationException(ErrorType.LINK, log);
        }

        return new ShaderProgram(program);
    }

    public static ShaderProgram load(ResourceLocation vertexShader, ResourceLocation fragmentShader)
            throws ProgramCreationException {
        try {
            return load(IOUtils.toString(Utils.getResourceStream(vertexShader)),
                    IOUtils.toString(Utils.getResourceStream(fragmentShader)));
        } catch (Exception ex) {
            throw Throwables.propagate(ex);
        }
    }

    private static void linkShader(int program, int shaderType, String source) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, source);
        glCompileShader(shader);

        int res = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (res == GL_FALSE) {
            String log = glGetShaderInfoLog(shader, glGetShaderi(shader, GL_INFO_LOG_LENGTH));
            throw new ProgramCreationException(ErrorType.COMPILE, log);
        }

        glAttachShader(program, shader);
    }

    private final int programID;

    private ShaderProgram(int shaderID) {
        this.programID = shaderID;
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        glDeleteProgram(programID);
    }

    public int getProgramID() {
        return programID;
    }

}
