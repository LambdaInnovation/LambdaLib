/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.entityx.handlers;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import cn.lambdalib.util.entityx.MotionHandler;
import cn.lambdalib.util.entityx.event.CollideEvent;
import cn.lambdalib.util.generic.VecUtils;
import cn.lambdalib.util.mc.BlockSelectors;
import cn.lambdalib.util.mc.IBlockSelector;
import cn.lambdalib.util.mc.Raytrace;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

/**
 * Rigidbody will update velocity and apply gravity and do simple collision.
 * @author WeAthFolD
 */
public class Rigidbody extends MotionHandler {
    
    public double gravity = 0.00; //block/tick^2
    public double linearDrag = 1.0;
    
    public Predicate<Entity> entitySel;
    public IBlockSelector blockFil = BlockSelectors.filNormal;
    
    public boolean accurateCollision = false;

    @Override
    public String getID() {
        return "Rigidbody";
    }

    @Override
    public void onStart() {}

    @Override
    public void onUpdate() {
        Entity target = getTarget();
        
        //Collision detection
        MovingObjectPosition mop = null;
        if(accurateCollision) {
            float hw = target.width / 2, hh = target.height;
            Vec3[] points = {
                VecUtils.vec(target.posX - hw, target.posY,      target.posZ - hw),
                VecUtils.vec(target.posX - hw, target.posY,      target.posZ + hw),
                VecUtils.vec(target.posX + hw, target.posY,      target.posZ + hw),
                VecUtils.vec(target.posX + hw, target.posY,      target.posZ - hw),
                VecUtils.vec(target.posX - hw, target.posY + hh, target.posZ - hw),
                VecUtils.vec(target.posX - hw, target.posY + hh, target.posZ + hw),
                VecUtils.vec(target.posX + hw, target.posY + hh, target.posZ + hw),
                VecUtils.vec(target.posX + hw, target.posY + hh, target.posZ - hw),
            };
            Vec3d motion = VecUtils.vec(target.motionX, target.motionY, target.motionZ);
            for(Vec3 vec : points) {
                Vec3 next = VecUtils.add(vec, motion);
                if((mop = Raytrace.perform(target.worldObj, vec, next, entitySel, blockFil)) != null)
                    break;
            }
        } else {
            Vec3 cur = Vec3.createVectorHelper(target.posX, target.posY, target.posZ),
                    next = Vec3.createVectorHelper(target.posX + target.motionX, target.posY + target.motionY, target.posZ + target.motionZ);
            mop = Raytrace.perform(target.worldObj, cur, next, entitySel, blockFil);
        }
        
        if(mop != null) {
            getEntityX().postEvent(new CollideEvent(mop)); //Let the event handlers do the actual job.
        }
        
        //Velocity update
        target.motionY -= gravity;
        
        target.motionX *= linearDrag;
        target.motionY *= linearDrag;
        target.motionZ *= linearDrag;
        
        target.lastTickPosX = target.posX;
        target.lastTickPosY = target.posY;
        target.lastTickPosZ = target.posZ;
        target.setPosition(target.posX + target.motionX, target.posY + target.motionY, target.posZ + target.motionZ);
    }

}
