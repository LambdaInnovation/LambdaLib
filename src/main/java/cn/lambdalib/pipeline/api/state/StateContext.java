package cn.lambdalib.pipeline.api.state;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class StateContext {

    public enum AlphaTestMode {
        Less(GL_LESS), LessEqual(GL_LEQUAL), Equal(GL_EQUAL),
        Greater(GL_GREATER), GreaterEqual(GL_GEQUAL);

        final int func;

        AlphaTestMode(int glValue) {
            this.func = glValue;
        }
    }

    public enum CullFaceMode {
        Front(GL_FRONT), Back(GL_BACK);

        final int func;

        CullFaceMode(int func) {
            this.func = func;
        }
    }

    public enum BlendFunction {
        SrcAlpha(GL_SRC_ALPHA), OneMinusSrcAlpha(GL_ONE_MINUS_SRC_ALPHA),
        SrcColor(GL_SRC_COLOR), OneMinusSrcColor(GL_ONE_MINUS_SRC_COLOR),
        DstAlpha(GL_DST_ALPHA), OneMinusDstAlpha(GL_ONE_MINUS_DST_ALPHA),
        DstColor(GL_DST_COLOR), OneMinusDstColor(GL_ONE_MINUS_DST_COLOR),
        One(GL_ONE), Zero(GL_ZERO);

        final int func;

        BlendFunction(int func) {
            this.func = func;
        }
    }

    private boolean a_enabled = true;
    private AlphaTestMode a_mode = AlphaTestMode.GreaterEqual;
    private float a_ref = 0.1f;

    private boolean cf_enabled = true;
    private CullFaceMode cf_mode = CullFaceMode.Back;

    private boolean blend_enabled = true;
    private BlendFunction blend_src_func = BlendFunction.SrcAlpha, blend_dst_func = BlendFunction.OneMinusSrcAlpha;

    private Map<Integer, Integer> texBind2D = new HashMap<>();

    public void setAlphaTestEnabled(boolean enabled) {
        a_enabled = enabled;
    }

    public void setAlphaTest(AlphaTestMode mode, float threshold) {
        a_mode = mode;
        a_ref = threshold;
    }

    public void setCullFaceEnabled(boolean enabled) {
        cf_enabled = enabled;
    }

    public void setCullFace(CullFaceMode mode) {
        cf_mode = mode;
    }

    public void setTexBinding2D(int binding, int textureID) {
        texBind2D.put(binding, textureID);
    }

    public void setTexBinding2D(int binding, ResourceLocation texture) {
        TextureManager texManager = Minecraft.getMinecraft().getTextureManager();
        ITextureObject obj = texManager.getTexture(texture);
        if (obj == null) {
            obj = new SimpleTexture(texture);
            texManager.loadTexture(texture, obj);
        }

        setTexBinding2D(binding, obj.getGlTextureId());
    }

    public void setBlendEnabled(boolean enabled) {
        blend_enabled = enabled;
    }

    public void setBlendFunc(BlendFunction srcFunc, BlendFunction dstFunc) {
        blend_src_func = srcFunc;
        blend_dst_func = dstFunc;
    }

    public void apply() {
        if (a_enabled) {
            glEnable(GL_ALPHA_TEST);
            glAlphaFunc(a_mode.func, a_ref);
        } else {
            glDisable(GL_ALPHA_TEST);
        }

        if (cf_enabled) {
            glEnable(GL_CULL_FACE);
            glCullFace(cf_mode.func);
        } else {
            glDisable(GL_CULL_FACE);
        }

        if (blend_enabled) {
            glEnable(GL_BLEND);
            glBlendFunc(blend_src_func.func, blend_dst_func.func);
        } else {
            glDisable(GL_BLEND);
        }

        if (!texBind2D.isEmpty()) {
            for (Entry<Integer, Integer> entry : texBind2D.entrySet()) {
                int binding = entry.getKey();
                int texture = entry.getValue();
                glActiveTexture(GL_TEXTURE0 + binding);
                glBindTexture(GL_TEXTURE_2D, texture);
            }
            glActiveTexture(GL_TEXTURE0);
        }
    }

}
