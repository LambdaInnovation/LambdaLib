/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.particle;

import java.util.ArrayList;
import java.util.List;

import cn.lambdalib.particle.decorators.ParticleDecorator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * TODO Completely redesign the particle system
 * 
 * @author WeAthFolD
 */
public class ParticleFactory extends ParticleFactoryBase {

    public final Particle template;

    private List<ParticleDecorator> decorators = new ArrayList();

    double px, py, pz;
    double vx, vy, vz;

    public ParticleFactory(Particle _template) {
        template = _template;
    }

    public void addDecorator(ParticleDecorator dec) {
        decorators.add(dec);
    }

    public void setPosition(double x, double y, double z) {
        px = x;
        py = y;
        pz = z;
    }

    public void setVelocity(double x, double y, double z) {
        vx = x;
        vy = y;
        vz = z;
    }

    /**
     * Generate a specific particle in the world with the current position and
     * velocity settings.
     */
    @Override
    public Particle next(World world) {
        Particle p = this.queryParticle();
        p.world = world;
        p.fromTemplate(template);

        p.setPosition(px, py, pz);

        p.motionX = vx;
        p.motionY = vy;
        p.motionZ = vz;

        for (ParticleDecorator pd : decorators)
            pd.decorate(p);

        return p;
    }

    /**
     * Generates a particle at the given world position with the given velocity.
     * Note that this will override the current position and velocity settings.
     */
    public Particle next(World world, BlockPos position, BlockPos velocity) {
        px = position.getX();
        py = position.getY();
        pz = position.getZ();

        vx = velocity.getX();
        vy = velocity.getY();
        vz = velocity.getZ();

        return next(world);
    }

}
