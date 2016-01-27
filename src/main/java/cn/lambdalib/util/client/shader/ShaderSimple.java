/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client.shader;

import org.lwjgl.opengl.GL20;

/**
 * @author WeAthFolD
 */
public class ShaderSimple extends ShaderProgram {
    private static ShaderSimple instance;
    
    public static ShaderSimple instance() {
        if(instance == null)
            instance = new ShaderSimple();
        return instance;
    }
    
    private ShaderSimple() {
        this.linkShader(getShader("simple.vert"), GL20.GL_VERTEX_SHADER);
        this.linkShader(getShader("simple.frag"), GL20.GL_FRAGMENT_SHADER);
        this.compile();
        
        this.useProgram();
        GL20.glUniform1i(GL20.glGetUniformLocation(this.getProgramID(), "sampler"), 0);
        GL20.glUseProgram(0);
    }
}
