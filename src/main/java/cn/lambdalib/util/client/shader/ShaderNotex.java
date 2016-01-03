/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
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
