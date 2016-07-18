package cn.lambdalib.pipeline.api.particle;

public interface ParticleAffector {

    /**
     * Called when a particle is added into given {@link ParticleSystem}.
     */
    default void init(ParticleSystem system, Particle particle) {

    }

    /**
     * Run the tick update for the affector.
     * @param system the particle system updated on.
     * @param deltaTime time passed in second last frame.
     */
    void update(ParticleSystem system, float deltaTime);

    /**
     * @return Whether this affector is still valid. Invalid affector will be removed on frame update.
     */
    default boolean isValid() {
        return true;
    }

}
