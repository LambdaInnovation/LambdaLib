/**
 * Copyright (c) Lambda Innovation, 2013-2015
 * 本作品版权由Lambda Innovation所有。
 * http://www.li-dev.cn/
 *
 * This project is open-source, and it is distributed under  
 * the terms of GNU General Public License. You can modify
 * and distribute freely as long as you follow the license.
 * 本项目是一个开源项目，且遵循GNU通用公共授权协议。
 * 在遵照该协议的情况下，您可以自由传播和修改。
 * http://www.gnu.org/licenses/gpl.html
 */
package cn.lambdalib.particle;

import java.util.ArrayList;
import java.util.List;

import cn.lambdalib.particle.decorators.ParticleDecorator;
import net.minecraft.util.Vec3;
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
        p.worldObj = world;
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
    public Particle next(World world, Vec3 position, Vec3 velocity) {
        px = position.xCoord;
        py = position.yCoord;
        pz = position.zCoord;

        vx = velocity.xCoord;
        vy = velocity.yCoord;
        vz = velocity.zCoord;

        return next(world);
    }

}
