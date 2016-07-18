package cn.lambdalib.pipeline.api.particle;

import cn.lambdalib.pipeline.api.GraphicPipeline;
import cn.lambdalib.pipeline.api.Material;
import cn.lambdalib.pipeline.api.Material.Layout;
import cn.lambdalib.pipeline.api.Material.Mesh;
import cn.lambdalib.pipeline.api.Material.MeshType;
import cn.lambdalib.pipeline.api.Material.Vertex;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ParticleSystem {

    public static Builder builder(Material material) {
        return new Builder(material);
    }

    public enum RenderMode {
        Billboard,
        VerticalBillboard,
        HorizontalBillboard
    }

    private final Material material;
    private final RenderMode mode;
    private final List<ParticleAffector> affectors;
    private final List<Particle> particles = new ArrayList<>();

    private final Mesh mesh;

    private final Matrix4f viewMatrix = new Matrix4f();

    private final Layout l_viewMatrix;

    public ParticleSystem(Material material, RenderMode mode, List<ParticleAffector> affectors) {
        this.material = material;
        this.mode = mode;
        this.affectors = affectors;

        mesh = material.newMesh(MeshType.STATIC);

        l_viewMatrix = material.getLayout("viewMatrix");

        float s = 0.5f;
        mesh.setVertices(
                vert(-s, -s),
                vert(-s,  s),
                vert( s,  s),
                vert( s, -s)
        );
    }

    private Vertex vert(float x, float y) {
        Layout l_position = material.getLayout("position"), l_uv = material.getLayout("uv");

        return material.newVertex()
                .setVec3(l_position, x, y, 0)
                .setVec2(l_uv, x, y);
    }

    public void setViewMatrix(Matrix4f viewMatrix) {
        this.viewMatrix.set(viewMatrix);
    }

    /**
     * Adds a particle.
     */
    public void add(Particle particle) {
        affectors.forEach(a -> a.init(this, particle));

        particles.add(particle);
    }

    /**
     * @return A view of current particles. The removal of list elements is prohibited and leads to undefined result.
     */
    public List<Particle> getParticles() {
        return particles;
    }


    /**
     * Runs the update for this ParticleSystem. All {@link ParticleAffector}s will be updated and removed if invalid.
     * @param deltaTime deltaTime of last frame
     */
    public void update(float deltaTime) {
        particles.forEach(p -> p.update(deltaTime));

        affectors.forEach(affector -> affector.update(this, deltaTime));

        Iterator<Particle> iter = particles.iterator();
        while (iter.hasNext()) {
            Particle p = iter.next();
            if (p.dead) {
                iter.remove();
            }
        }
    }

    /**
     * Forwards the rendering data of this particle system to given {@link GraphicPipeline}.
     */
    public void render(GraphicPipeline target) {
        for (Particle p : particles) {
            target.draw(material, mesh, material.newInstance());
        }
    }


    public static class Builder {

        private final Material material;
        private RenderMode mode = RenderMode.Billboard;
        private final List<ParticleAffector> affectors = new LinkedList<>();

        public Builder(Material material) {
            this.material = material;
        }

        public Builder renderMode(RenderMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder affector(ParticleAffector affector) {
            affectors.add(affector);
            return this;
        }

        public ParticleSystem build() {
            return new ParticleSystem(material, mode, affectors);
        }

    }

}
