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
package cn.liutils.util.client.shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import cn.liutils.core.LIUtils;
import cn.liutils.util.generic.RegistryUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;

/**
 * A simple GL Shader Program wrapper.
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
public class ShaderProgram {
	
	static Map<ResourceLocation, Integer> loadedShaders = new HashMap();
	public static void releaseResources() {
		for(Integer e : loadedShaders.values())
			glDeleteShader(e);
		loadedShaders.clear();
	}
	
	private boolean compiled = false;
	private int programID;
	private List<Integer> attachedShaders = new ArrayList();
	
	public ShaderProgram() {
		programID = glCreateProgram();
	}
	
	public void linkShader(ResourceLocation location, int type) {
		try {
			int shaderID;
			if(loadedShaders.containsKey(location)) {
				shaderID = loadedShaders.get(location);
			} else {
				String str = IOUtils.toString(RegistryUtils.getResourceStream(location));
				shaderID = glCreateShader(type);
				glShaderSource(shaderID, str);
				glCompileShader(shaderID);
				
				int successful = glGetShaderi(shaderID, GL_COMPILE_STATUS);
				if(successful == GL_FALSE) {
					String log = glGetShaderInfoLog(shaderID, glGetShaderi(shaderID, GL_INFO_LOG_LENGTH));
					LIUtils.log.error("Error when linking shader '" + location + "'. code: " + successful + ", Error string: \n" + log);
					throw new RuntimeException();
				}
			}
			
			attachedShaders.add(shaderID);
			glAttachShader(programID, shaderID);
		} catch (IOException e) {
			LIUtils.log.error("Error when linking shader " + location, e);
			throw new RuntimeException();
		}
	}
	
	public int getProgramID() {
		return programID;
	}
	
	public void useProgram() {
		if(compiled) {
			glUseProgram(programID);
		} else {
			LIUtils.log.error("Trying to use a uncompiled program");
			throw new RuntimeException();
		}
	}
	
	public int getUniformLocation(String name) {
		return glGetUniformLocation(getProgramID(), name);
	}
	
	public void compile() {
		if(compiled) {
			LIUtils.log.error("Trying to compile shader " + this + " twice.");
			throw new RuntimeException();
		}
		
		glLinkProgram(programID);
		
		for(Integer i : attachedShaders)
			glDetachShader(programID, i);
		attachedShaders = null;
		
		int status = glGetProgrami(programID, GL_LINK_STATUS);
		if(status == GL_FALSE) {
			String log = glGetProgramInfoLog(programID, glGetProgrami(programID, GL_INFO_LOG_LENGTH));
			LIUtils.log.error("Error when linking program #" + programID + ". Error code: " + status + ", Error string: ");
			LIUtils.log.error(log);
			throw new RuntimeException();
		}
		
		compiled = true;
	}
	
	/**
	 * Get the src of a shader in liutils namespace.
	 * @param name
	 * @return
	 */
	public static ResourceLocation getShader(String name) {
		return new ResourceLocation("liutils:shaders/" + name);
	}
	
}
