package cn.lambdalib.pipeline.api.particle;

import cn.lambdalib.util.helper.Color;
import org.lwjgl.util.vector.Vector3f;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Particle {

    /**
     * Interface for particle attribute.
     */
    public static abstract class Attr {

        Particle particle;

        public abstract void update(float deltaTime);

        public Particle getParticle() {
            return particle;
        }

    }

    public final Vector3f pos, velocity;

    public float size;

    public final Color color;

    public boolean dead;

    private final List<Attr> attributes = new ArrayList<>();

    Particle(Vector3f pos, Vector3f velocity, Color color, float size) {
        this.pos = pos;
        this.velocity = velocity;
        this.color = color;
        this.size = size;
        this.dead = false;
    }

    void update(float deltaTime) {
        attributes.forEach(attr -> attr.update(deltaTime));
    }

    public void addAttr(Attr attr) {
        attributes.add(attr);
    }

    /**
     * Gets an attribute of given type.
     * @return An instance of type T
     * @throws InvalidStateException if no attribute of such type
     */
    public <T extends Attr> T getAttr(Class<T> type) {
        for (Attr a : attributes) {
            if (type.isInstance(a)) {
                @SuppressWarnings("unchecked")
                T ret = (T) a;
                return ret;
            }
        }
        throw new InvalidStateException("No attribute of " + type + " present");
    }

    /**
     * Finds an attribute of given type.
     */
    public <T extends Attr> Optional<T> findAttr(Class<T> type) {
        @SuppressWarnings("unchecked")
        Optional<T> ret = (Optional<T>) attributes.stream().filter(type::isInstance).findAny();

        return ret;
    }


}
