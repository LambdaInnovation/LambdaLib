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
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

/**
 * Some commonly used vector calculation & generations.
 * @author WeAthFolD
 */
public class VecUtils {
    private static Random rand = new Random();
    
    public static Vec3 vec(double x, double y, double z) {
        return Vec3.createVectorHelper(x, y, z);
    }
    
    public static Vec3 random() {
        return vec(-1 + 2 * rand.nextDouble(), -1 + 2 * rand.nextDouble(), -1 + 2 * rand.nextDouble());
    }
    
    /**
     * Convert the yaw and pitch angle to the looking direction vector
     * @param yaw in mc entity angle space
     * @param pitch in mc entity angle space
     * @return the looking direction vec, normalized
     */
    public static Vec3 toDirVector(float yaw, float pitch) {
        float f1 = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f3 = -MathHelper.cos(-pitch * 0.017453292F);
        float f4 = MathHelper.sin(-pitch * 0.017453292F);
        return Vec3.createVectorHelper((double)(f2 * f3), (double)f4, (double)(f1 * f3));
    }
    
    /**
     * FIXME <b>Currently BUGGY.</b><br>
     * Get the closest point on line AB to point P. Calc is based on fact that vecPH*vecAB=0, along with linear interploation.
     * @param p Targe point
     * @param a One point in line segment
     * @param b Another point in line segment, must not equal to a
     * @return The closest point on AB.
     * 
     */
    public static Vec3 getClosestPointOn(Vec3 p, Vec3 a, Vec3 b) {
        double x0 = a.xCoord, y0 = a.yCoord, z0 = a.zCoord,
                x1 = b.xCoord, y1 = b.yCoord, z1 = b.zCoord;
        double X = p.xCoord, Y = p.yCoord, Z = p.zCoord;
        
        double dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        
        double mid1 = dx * dx + dy * dy + dz * dz;
        double mid2 = dx * (x1 - X) + dy * (y1 - Y) + dz * (z1 - Z);
        
        double lambda = mid2 / mid1;
        
        return lerp(a, b, lambda);
    }
    
    public static Vec3 multiply(Vec3 v, double scale) {
        return Vec3.createVectorHelper(v.xCoord * scale, v.yCoord * scale, v.zCoord * scale);
    }
    
    public static Vec3 lerp(Vec3 a, Vec3 b, double lambda) {
        return Vec3.createVectorHelper(
                MathUtils.lerp(a.xCoord, b.xCoord, lambda),
                MathUtils.lerp(a.yCoord, b.yCoord, lambda),
                MathUtils.lerp(a.zCoord, b.zCoord, lambda));
    }
    
    public static Vec3 neg(Vec3 v) {
        return Vec3.createVectorHelper(-v.xCoord, -v.yCoord, -v.zCoord);
    }
    
    public static Vec3 add(Vec3 a, Vec3 b) {
        return Vec3.createVectorHelper(a.xCoord + b.xCoord, a.yCoord + b.yCoord, a.zCoord + b.zCoord);
    }
    
    public static Vec3 subtract(Vec3 a, Vec3 b) {
        return add(a, neg(b));
    }
    
    public static double magnitudeSq(Vec3 a) {
        return a.xCoord * a.xCoord + a.yCoord * a.yCoord + a.zCoord * a.zCoord;
    }
    
    public static double magnitude(Vec3 a) {
        return Math.sqrt(magnitudeSq(a));
    }
    
    public static Vec3 copy(Vec3 v) {
        return Vec3.createVectorHelper(v.xCoord, v.yCoord, v.zCoord);
    }
    
    public static void copy(Vec3 from, Vec3 to) {
        to.xCoord = from.xCoord;
        to.yCoord = from.yCoord;
        to.zCoord = from.zCoord;
    }
    
    public static Vec3 crossProduct(Vec3 a, Vec3 b) {
        double 
            x0 = a.xCoord, y0 = a.yCoord, z0 = a.zCoord,
            x1 = b.xCoord, y1 = b.yCoord, z1 = b.zCoord;
        return Vec3.createVectorHelper(
            y0 * z1 - y1 * z0, 
            x1 * z0 - x0 * z1, 
            x0 * y1 - x1 * y0);
    }

    // CREDITS TO Greg S for the original code.
    private static Vec3 getIntersection(double fDst1, double fDst2, Vec3 P1, Vec3 P2) {
        if ( (fDst1 * fDst2) >= 0.0f) return null;
        if ( fDst1 == fDst2) return null; 
        return add(P1, multiply(subtract(P2, P1), ( -fDst1 / (fDst2-fDst1) )));
    }
    
    private static boolean inBox(Vec3 Hit, Vec3 B1, Vec3 B2, int Axis) {
        if ( Axis==1 && Hit.zCoord > B1.zCoord && Hit.zCoord < B2.zCoord && Hit.yCoord > B1.yCoord && Hit.yCoord < B2.yCoord) return true;
        if ( Axis==2 && Hit.zCoord > B1.zCoord && Hit.zCoord < B2.zCoord && Hit.xCoord > B1.xCoord && Hit.xCoord < B2.xCoord) return true;
        if ( Axis==3 && Hit.xCoord > B1.xCoord && Hit.xCoord < B2.xCoord && Hit.yCoord > B1.yCoord && Hit.yCoord < B2.yCoord) return true;
        return false;
    }

    public static Vec3 checkLineAABB(Vec3 L1, Vec3 L2, AxisAlignedBB aabb) {
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
    public static Vec3 checkLineBox(Vec3 B1, Vec3 B2, Vec3 L1, Vec3 L2) {
        if (L2.xCoord < B1.xCoord && L1.xCoord < B1.xCoord) return null;
        if (L2.xCoord > B2.xCoord && L1.xCoord > B2.xCoord) return null;
        if (L2.yCoord < B1.yCoord && L1.yCoord < B1.yCoord) return null;
        if (L2.yCoord > B2.yCoord && L1.yCoord > B2.yCoord) return null;
        if (L2.zCoord < B1.zCoord && L1.zCoord < B1.zCoord) return null;
        if (L2.zCoord > B2.zCoord && L1.zCoord > B2.zCoord) return null;
        
        if (L1.xCoord > B1.xCoord && L1.xCoord < B2.xCoord &&
            L1.yCoord > B1.yCoord && L1.yCoord < B2.yCoord &&
            L1.zCoord > B1.zCoord && L1.zCoord < B2.zCoord) 
            return L1;
        
        Vec3 Hit;
        if ( ((Hit = getIntersection(L1.xCoord-B1.xCoord, L2.xCoord-B1.xCoord, L1, L2)) != null && inBox( Hit, B1, B2, 1 ))
          || ((Hit = getIntersection( L1.yCoord-B1.yCoord, L2.yCoord-B1.yCoord, L1, L2)) != null && inBox( Hit, B1, B2, 2 )) 
          || ((Hit = getIntersection( L1.zCoord-B1.zCoord, L2.zCoord-B1.zCoord, L1, L2)) != null && inBox( Hit, B1, B2, 3 )) 
          || ((Hit = getIntersection( L1.xCoord-B2.xCoord, L2.xCoord-B2.xCoord, L1, L2)) != null && inBox( Hit, B1, B2, 1 )) 
          || ((Hit = getIntersection( L1.yCoord-B2.yCoord, L2.yCoord-B2.yCoord, L1, L2)) != null && inBox( Hit, B1, B2, 2 )) 
          || ((Hit = getIntersection( L1.zCoord-B2.zCoord, L2.zCoord-B2.zCoord, L1, L2)) != null && inBox( Hit, B1, B2, 3 )))
            return Hit;

        return null;
    }
    
    public static Vec3 entityPos(Entity e) {
        return vec(e.posX, e.posY + (isThePlayer(e) ? -1.6 : 0.0), e.posZ);
    }

    public static Vec3 entityHeadPos(Entity e) {
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
        return Minecraft.getMinecraft().thePlayer.equals(e);
    }
    
    public static Vec3 entityMotion(Entity e) {
        return vec(e.motionX, e.motionY, e.motionZ);
    }
    
}
