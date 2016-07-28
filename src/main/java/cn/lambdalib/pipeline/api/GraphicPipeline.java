package cn.lambdalib.pipeline.api;


import cn.lambdalib.pipeline.api.state.StateContext;
import cn.lambdalib.pipeline.core.GLBuffer;
import cn.lambdalib.pipeline.core.VAO;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class GraphicPipeline {

    private Map<Material, Multimap<SubGroup, Material.Instance>> drawCalls = new HashMap<>();

    private final StateContext context = new StateContext();

    public void draw(Material mat, Material.Mesh mesh, Material.Instance instance) {
        put(mat, new SubGroup(mesh, true, -1), instance);
    }

    public void draw(Material mat, Material.Mesh mesh, Material.Instance instance, int subMeshIndex) {
        put(mat, new SubGroup(mesh, false, subMeshIndex), instance);
    }

    /**
     * @return The state context that will serve as the default value when this pipeline {@link #flush}s.
     */
    public StateContext stateContext() {
        return context;
    }

    private void put(Material mat, SubGroup group, Material.Instance instance) {
        if (!drawCalls.containsKey(mat)) {
            drawCalls.put(mat, ArrayListMultimap.create());
        }

        drawCalls.get(mat).put(group, instance);
    }

    public void flush() {
        for (Material mat : drawCalls.keySet()) {
            glUseProgram(mat.program.getProgramID());
            // --------- Update states and uniforms -----------
            context.apply();
            mat.stateContext().apply();
            mat.updateUniforms();
            // -------------------------------------------------

            Multimap<SubGroup, Material.Instance> thisMatDraws = drawCalls.get(mat);

            GLBuffer instanceVBO = GLBuffer.create();
            VAO vao = VAO.create();
            glBindVertexArray(vao.getID());

            // Per-instance data pointer
            glBindBuffer(GL_ARRAY_BUFFER, instanceVBO.getID());

            for (Material.Layout layout : mat.instanceLayouts) {
                glEnableVertexAttribArray(layout.location);
                glVertexAttribPointer(layout.location, layout.attrType.dimension, GL_FLOAT, false,
                        mat.instanceFloats * 4,
                        layout.floatPadding * 4);
                glVertexAttribDivisor(layout.location, 1);
            }

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            for (SubGroup group : thisMatDraws.keySet()) {
                Material.Mesh mesh = group.mesh;
                Collection<Material.Instance> instances = thisMatDraws.get(group);

                // Per-vertex data pointer
                glBindBuffer(GL_ARRAY_BUFFER, mesh.getVBO().getID());
                for (Material.Layout layout : mat.vertexLayouts) {
                    glEnableVertexAttribArray(layout.location);
                    glVertexAttribPointer(layout.location, layout.attrType.dimension, GL_FLOAT, false,
                            mat.vertexFloats * 4,
                            layout.floatPadding * 4);
                    glVertexAttribDivisor(layout.location, 0);
                }
                glBindBuffer(GL_ARRAY_BUFFER, 0);

                // Update instance bufffer
                FloatBuffer buffer = BufferUtils.createFloatBuffer(mat.instanceFloats * instances.size());
                for (Material.Instance i : instances) {
                    buffer.put(i.values);
                }
                buffer.flip();

                glBindBuffer(GL_ARRAY_BUFFER, instanceVBO.getID());
                glBufferData(GL_ARRAY_BUFFER, buffer, GL_DYNAMIC_DRAW);
                glBindBuffer(GL_ARRAY_BUFFER, 0);

                // Bind IBO
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, group.getIBO().getID());

                glDrawElementsInstanced(GL_TRIANGLES, mesh.getIndiceCount(), GL_UNSIGNED_INT, 0, instances.size());

                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            }

            glBindVertexArray(0);
            glUseProgram(0);
        }

        drawCalls.clear();
    }

    private class SubGroup {

        final Material.Mesh mesh;
        final boolean all;
        final int iboID;

        SubGroup(Material.Mesh mesh, boolean all, int iboID) {
            this.mesh = mesh;
            this.all = all;
            this.iboID = iboID;
        }

        GLBuffer getIBO() {
            return all ? mesh.getIBOAll() : mesh.getIBOSub(iboID);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SubGroup subGroup = (SubGroup) o;

            if (all != subGroup.all) return false;
            if (iboID != subGroup.iboID) return false;
            return mesh != null ? mesh.equals(subGroup.mesh) : subGroup.mesh == null;
        }

        @Override
        public int hashCode() {
            int result = mesh != null ? mesh.hashCode() : 0;
            result = 31 * result + (all ? 1 : 0);
            result = 31 * result + iboID;
            return result;
        }
    }


}
