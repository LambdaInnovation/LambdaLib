/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.generic;

import java.util.Random;

import cn.lambdalib.util.mc.SideHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Some commonly used vector calculation & generations.
 * @author WeAthFolD
 */
public class VecUtils {
    private static Random rand = new Random();

    /**
     * In 1.8 or above, Vec support construction function, Vec.create...(x,y,z) has been removed.
     */
    public static Vec3d vec(double x, double y, double z) {
        return new Vec3d(x, y, z);
    }
    
    public static Vec3d random() {
        return new Vec3d(-1 + 2 * rand.nextDouble(), -1 + 2 * rand.nextDouble(), -1 + 2 * rand.nextDouble());
    }
    
    /**
     * Convert the yaw and pitch angle to the looking direction vector
     * @param yaw in mc entity angle space
     * @param pitch in mc entity angle space
     * @return the looking direction vec, normalized
     */
    public static Vec3d toDirVector(float yaw, float pitch) {
        float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float f4 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d((double)(f2 * f3), (double)f4, (double)(f1 * f3));
    }
    
    /**
     * TODO FIXME <b>Currently BUGGY.</b><br>
     * Get the closest point on line AB to point P. Calc is based on fact that vecPH*vecAB=0, along with linear interploation.
     * @param p Targe point
     * @param a One point in line segment
     * @param b Another point in line segment, must not equal to a
     * @return The closest point on AB.
     * 
     */
    public static Vec3d getClosestPointOn(Vec3d p, Vec3d a, Vec3d b) {
        double x0 = a.x, y0 = a.y, z0 = a.z,
                x1 = b.x, y1 = b.y, z1 = b.z;
        double X = p.x, Y = p.y, Z = p.z;
        
        double dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        
        double mid1 = dx * dx + dy * dy + dz * dz;
        double mid2 = dx * (x1 - X) + dy * (y1 - Y) + dz * (z1 - Z);
        
        double lambda = mid2 / mid1;
        
        return lerp(a, b, lambda);
    }

    public static Vec3d multiply(Vec3d v, double scale) {
        return new Vec3d(v.x * scale, v.y * scale, v.z * scale);
    }


    public static Vec3d lerp(Vec3d a, Vec3d b, double lambda) {
        return new Vec3d(
                MathUtils.lerp(a.x, b.x, lambda),
                MathUtils.lerp(a.y, b.y, lambda),
                MathUtils.lerp(a.z, b.z, lambda));
    }


    public static Vec3d neg(Vec3d v) {
        return new Vec3d(-v.x, -v.y, -v.z);
    }
    
    public static Vec3d add(Vec3d a, Vec3d b) {
        return new Vec3d(a.x + b.x, a.y + b.y, a.z + b.z);
    }
    
    public static Vec3d subtract(Vec3d a, Vec3d b) {
        return new Vec3d(a.x-b.x,a.y-b.y,a.z-b.z);
    }
    
    public static double magnitudeSq(Vec3d a) {
        return a.x * a.x + a.y * a.y + a.z * a.z;
    }
    
    public static double magnitude(Vec3d a) {
        return Math.sqrt(magnitudeSq(a));
    }
    
    public static Vec3d copy(Vec3d v) {
        return new Vec3d(v.x, v.y, v.z);
    }
    
    public static Vec3d crossProduct(Vec3d a, Vec3d b) {
        double 
            x0 = a.x, y0 = a.y, z0 = a.z,
            x1 = b.x, y1 = b.y, z1 = b.z;
        return new Vec3d(
            y0 * z1 - y1 * z0, 
            x1 * z0 - x0 * z1, 
            x0 * y1 - x1 * y0);
    }

    // CREDITS TO Greg S for the original code.
    private static Vec3d getIntersection(double fDst1, double fDst2, Vec3d P1, Vec3d P2) {
        if ( (fDst1 * fDst2) >= 0.0f) return null;
        if ( fDst1 == fDst2) return null; 
        return add(P1, multiply(subtract(P2, P1), ( -fDst1 / (fDst2-fDst1) )));
    }
    
    private static boolean inBox(Vec3d Hit, Vec3d B1, Vec3d B2, int Axis) {
        if ( Axis==1 && Hit.z > B1.z && Hit.z < B2.z && Hit.y > B1.y && Hit.y < B2.y) return true;
        if ( Axis==2 && Hit.z > B1.z && Hit.z < B2.z && Hit.x > B1.x && Hit.x < B2.x) return true;
        if ( Axis==3 && Hit.x > B1.x && Hit.x < B2.x && Hit.y > B1.y && Hit.y < B2.y) return true;
        return false;
    }

    public static Vec3d checkLineAABB(Vec3d L1, Vec3d L2, AxisAlignedBB aabb) {
        return checkLineBox(vec(aabb.minX, aabb.minY, aabb.minZ),
                vec(aabb.maxX, aabb.maxY, aabb.maxZ),
                L1, L2);
    }
    
    /**
     * Check if the line segment (L1, L2) intersects with AABB represented by (B1, B2).
     * If intersected, return the a hit point of the segment to the line.
     * Else, return null.
     * @param B1 smallest point for AABB
     * @param B2 largest point for AABB
     * @param L1 start point of the line
     * @param L2 end point of the line
     */
    public static Vec3d checkLineBox(Vec3d B1, Vec3d B2, Vec3d L1, Vec3d L2) {
        if (L2.x < B1.x && L1.x < B1.x) return null;
        if (L2.x > B2.x && L1.x > B2.x) return null;
        if (L2.y < B1.y && L1.y < B1.y) return null;
        if (L2.y > B2.y && L1.y > B2.y) return null;
        if (L2.z < B1.z && L1.z < B1.z) return null;
        if (L2.z > B2.z && L1.z > B2.z) return null;
        
        if (L1.x > B1.x && L1.x < B2.x &&
            L1.y > B1.y && L1.y < B2.y &&
            L1.z > B1.z && L1.z < B2.z) 
            return L1;
        
        Vec3d Hit;
        if ( ((Hit = getIntersection(L1.x-B1.x, L2.x-B1.x, L1, L2)) != null && inBox( Hit, B1, B2, 1 ))
          || ((Hit = getIntersection( L1.y-B1.y, L2.y-B1.y, L1, L2)) != null && inBox( Hit, B1, B2, 2 )) 
          || ((Hit = getIntersection( L1.z-B1.z, L2.z-B1.z, L1, L2)) != null && inBox( Hit, B1, B2, 3 )) 
          || ((Hit = getIntersection( L1.x-B2.x, L2.x-B2.x, L1, L2)) != null && inBox( Hit, B1, B2, 1 )) 
          || ((Hit = getIntersection( L1.y-B2.y, L2.y-B2.y, L1, L2)) != null && inBox( Hit, B1, B2, 2 )) 
          || ((Hit = getIntersection( L1.z-B2.z, L2.z-B2.z, L1, L2)) != null && inBox( Hit, B1, B2, 3 )))
            return Hit;

        return null;
    }
    
    public static Vec3d entityPos(Entity e) {
        return vec(e.posX, e.posY + (isThePlayer(e) ? -1.6 : 0.0), e.posZ);
    }

    public static Vec3d entityHeadPos(Entity e) {
        return vec(e.posX, e.posY + (isThePlayer(e) ? 0 : e.getEyeHeight()), e.posZ);
    }

    private static boolean isThePlayer(Entity e) {
        if (SideHelper.isClient()) {
            return isThePlayer_c(e);
        } else {
            return false;
        }
    }

    private static boolean isThePlayer_c(Entity e) {
        return Minecraft.getMinecraft().player.equals(e);
    }
    
    public static Vec3d entityMotion(Entity e) {
        return vec(e.motionX, e.motionY, e.motionZ);
    }
    
}
