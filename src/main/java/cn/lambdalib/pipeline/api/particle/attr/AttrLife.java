package cn.lambdalib.pipeline.api.particle.attr;

import cn.lambdalib.pipeline.api.particle.Particle.Attr;

public class AttrLife extends Attr {

    private float life;

    /**
     * @return The life of this particle in seconds.
     */
    public float getLife() {
        return life;
    }

    @Override
    public void update(float deltaTime) {
        life += deltaTime;
    }

}
