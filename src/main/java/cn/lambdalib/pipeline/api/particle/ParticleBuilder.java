package cn.lambdalib.pipeline.api.particle;

import cn.lambdalib.util.helper.Color;
import org.lwjgl.util.vector.Vector3f;

public class ParticleBuilder {

    public static ParticleBuilder create() {
        return new ParticleBuilder();
    }

    private final Vector3f
            pos = new Vector3f(),
            velocity = new Vector3f();

    private float size;

    private final Color color = new Color();

    public ParticleBuilder pos(float x, float y, float z) {
        pos.set(x, y, z);
        return this;
    }

    public ParticleBuilder pos(Vector3f v) {
        pos.set(v);
        return this;
    }

    public ParticleBuilder velocity(float x, float y, float z) {
        velocity.set(x, y, z);
        return this;
    }

    public ParticleBuilder velocity(Vector3f v) {
        velocity.set(v);
        return this;
    }

    public ParticleBuilder size(float sz) {
        size = sz;
        return this;
    }

    public ParticleBuilder color(Color c) {
        color.from(c);
        return this;
    }

    public Particle build() {
        return new Particle(
                new Vector3f(pos),
                new Vector3f(velocity),
                new Color().from(color),
                size);
    }

    private ParticleBuilder() {}

}
