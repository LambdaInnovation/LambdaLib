package cn.lambdalib.pipeline.api.mc;

import cn.lambdalib.pipeline.api.GraphicPipeline;
import cn.lambdalib.pipeline.core.RenderEventDispatch;
import com.google.common.base.Preconditions;
import net.minecraftforge.client.MinecraftForgeClient;

/**
 * Common graphic pipelines that injects into Minecraft rendering procedure.
 */
public class MCPipeline {

    /**
     * @return A graphic pipeline that flushes at the end of current entity render pass.
     * @throws IllegalStateException if not in entity render pass
     */
    public static GraphicPipeline entity() {
        Preconditions.checkState(MinecraftForgeClient.getRenderPass() != -1, "Not in entity render pass");

        return RenderEventDispatch.entityPipeline;
    }



}
