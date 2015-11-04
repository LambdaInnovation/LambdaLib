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

import org.lwjgl.opengl.GL20;

/**
 * @author WeAthFolD
 */
public class ShaderMono extends ShaderProgram {
	
	private static ShaderMono instance;
	
	public static ShaderMono instance() {
		if(instance == null)
			instance = new ShaderMono();
		return instance;
	}
	
	private ShaderMono() {
		this.linkShader(getShader("simple.vert"), GL20.GL_VERTEX_SHADER);
		this.linkShader(getShader("mono.frag"), GL20.GL_FRAGMENT_SHADER);
		this.compile();
		
		this.useProgram();
		GL20.glUniform1i(GL20.glGetUniformLocation(this.getProgramID(), "sampler"), 0);
		GL20.glUseProgram(0);
	}
	
}
