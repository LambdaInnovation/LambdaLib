/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client.shader;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL20;

import cn.lambdalib.util.helper.Color;

/**
 * @author WeAthFolD
 */
public class ShaderNotex extends ShaderProgram {
    
    private static ShaderNotex instance;
    
    public static ShaderNotex instance() {
        if(instance == null) {
            instance = new ShaderNotex();
        }
        return instance;
    }
    
    private ShaderNotex() {
        this.linkShader(getShader("simple.vert"), GL20.GL_VERTEX_SHADER);
        this.linkShader(getShader("notex.frag"), GL20.GL_FRAGMENT_SHADER);
        this.compile();
    }
    
}
