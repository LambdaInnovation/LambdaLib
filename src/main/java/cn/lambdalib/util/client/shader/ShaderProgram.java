/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.client.shader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;

import cn.lambdalib.core.LambdaLib;
import cn.lambdalib.util.generic.RegistryUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * A simple GL Shader Program wrapper.
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
public class ShaderProgram {
    
    private boolean compiled = false;
    private boolean valid = false;
    private int programID;
    private List<Integer> attachedShaders = new ArrayList<>();
    
    public ShaderProgram() {
        programID = glCreateProgram();
    }
    
    public void linkShader(ResourceLocation location, int type) {
        if (!checkCapability())
            return;

        try {
            boolean loaded;
            String str = IOUtils.toString(RegistryUtils.getResourceStream(location));
            int shaderID = glCreateShader(type);
            glShaderSource(shaderID, str);
            glCompileShader(shaderID);

            int successful = glGetShaderi(shaderID, GL_COMPILE_STATUS);
            if(successful == GL_FALSE) {
                String log = glGetShaderInfoLog(shaderID, glGetShaderi(shaderID, GL_INFO_LOG_LENGTH));
                LambdaLib.log.error("Error when linking shader '" + location + "'. code: " + successful + ", Error string: \n" + log);
                loaded = false;
            } else {
                loaded = true;
            }

            if (loaded) {
                attachedShaders.add(shaderID);
                glAttachShader(programID, shaderID);
            }
        } catch (IOException e) {
            LambdaLib.log.error("Didn't find shader " + location, e);
            Throwables.propagate(e);
        }
    }
    
    public int getProgramID() {
        return programID;
    }
    
    public void useProgram() {
        if(compiled && valid) {
            glUseProgram(programID);
        } else if (!compiled) {
            LambdaLib.log.error("Trying to use a uncompiled program");
            throw new RuntimeException();
        } // not valid, ignore the shader usage
    }
    
    public int getUniformLocation(String name) {
        return glGetUniformLocation(getProgramID(), name);
    }
    
    public void compile() {
        if (!checkCapability()) {
            compiled = true;
            return;
        }

        if(compiled) {
            LambdaLib.log.error("Trying to compile shader " + this + " twice.");
            throw new RuntimeException();
        }
        
        glLinkProgram(programID);
        
        for(Integer i : attachedShaders)
            glDetachShader(programID, i);
        attachedShaders = null;
        
        int status = glGetProgrami(programID, GL_LINK_STATUS);
        if(status == GL_FALSE) {
            String log = glGetProgramInfoLog(programID, glGetProgrami(programID, GL_INFO_LOG_LENGTH));
            LambdaLib.log.error("Error when linking program #" + programID + ". Error code: " + status + ", Error string: ");
            LambdaLib.log.error(log);
            valid = false;
        } else {
            valid = true;
        }
        
        compiled = true;
    }

    public boolean isValid() {
        return valid;
    }

    private boolean checkCapability() {
        String versionShort = GL11.glGetString(GL11.GL_VERSION).trim().substring(0, 3);
        return "2.1".compareTo(versionShort) <= 0;
    }
    
    /**
     * Get the src of a shader in lambdalib namespace.
     */
    public static ResourceLocation getShader(String name) {
        return new ResourceLocation("lambdalib:shaders/" + name);
    }
    
}
