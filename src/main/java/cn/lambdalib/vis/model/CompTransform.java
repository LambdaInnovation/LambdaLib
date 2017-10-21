/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.model;

import cn.lambdalib.s11n.SerializeType;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.generic.VecUtils;
@SerializeType
public class CompTransform {

    public static final CompTransform identity = new CompTransform();

    public Vec3d transform = new Vec3d(0, 0, 0);
    
    public Vec3d pivotPt = new Vec3d(0, 0, 0);
    
    public Vec3d rotation = new Vec3d(0, 0, 0);
    
    public double scale = 1.0;
    
    public CompTransform setPivot(double x, double y, double z) {
        svec(pivotPt, x, y, z);
        return this;
    }
    
    public CompTransform setTransform(double x, double y, double z) {
        svec(transform, x, y, z);
        return this;
    }
    
    public CompTransform setRotation(double x, double y, double z) {
        svec(rotation, x, y, z);
        return this;
    }
    
    public CompTransform setScale(double val) {
        scale = val;
        return this;
    }
    
    private void svec(Vec3d vec3, double x, double y, double z) {
        vec3=new Vec3d(x,y,z);
    }
    
    public void doTransform() {
        RenderUtils.glTranslate(VecUtils.add(transform, pivotPt));

        GL11.glRotated(rotation.x, 1, 0, 0);
        GL11.glRotated(rotation.t, 0, 1, 0);
        GL11.glRotated(rotation.z, 0, 0, 1);
        
        GL11.glScaled(scale, scale, scale);
        
        RenderUtils.glTranslate(VecUtils.neg(pivotPt));
    }
    
}
