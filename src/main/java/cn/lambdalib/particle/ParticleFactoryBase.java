/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.particle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.lambdalib.annoreg.core.Registrant;
import cn.lambdalib.annoreg.mc.RegEventHandler;
import cn.lambdalib.annoreg.mc.RegEventHandler.Bus;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author WeAthFolD
 */
@SideOnly(Side.CLIENT)
@Registrant
public abstract class ParticleFactoryBase {

    static final int MAX_POOL_SIZE = 1000;

    static List<Particle> alive = new ArrayList(), dead = new ArrayList();

    public abstract Particle next(World world);

    protected final Particle queryParticle() {
        Particle ret;
        if (!dead.isEmpty()) {
            Iterator<Particle> iter = dead.iterator();
            ret = iter.next();
            iter.remove();
        } else {
            ret = new Particle();
        }

        if (alive.size() < MAX_POOL_SIZE) {
            alive.add(ret);
        }

        ret.isDead = false;
        ret.ticksExisted = 0;
        ret.resetEntityX();
        ret.reset();
        return ret;
    }

    @Registrant
    @SideOnly(Side.CLIENT)
    public enum EventHandlers {
        @RegEventHandler(Bus.FML)
        instance;

        static final int UPDATE_RATE = 40;
        int ticker;

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END && ++ticker == UPDATE_RATE) {
                ticker = 0;

                Iterator<Particle> iter = alive.iterator();
                while (iter.hasNext()) {
                    Particle p = iter.next();
                    if (p.isDead) {
                        iter.remove();
                        if (dead.size() < MAX_POOL_SIZE) {
                            dead.add(p);
                        }
                    }
                }
                // System.out.println("GC: " + alive.size() + " / " +
                // dead.size());
            }
        }
    }

}
