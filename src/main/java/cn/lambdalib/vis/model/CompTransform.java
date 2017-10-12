/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.model;

import cn.lambdalib.s11n.SerializeType;
import org.lwjgl.opengl.GL11;

import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.generic.VecUtils;
import net.minecraft.util.Vec3;

@SerializeType
public class CompTransform {

    public static final CompTransform identity = new CompTransform();

    public Vec3 transform = new Vec3(0, 0, 0);
    
    public Vec3 pivotPt = new Vec3(0, 0, 0);
    
    public Vec3 rotation = new Vec3(0, 0, 0);
    
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
    
    private void svec(Vec3 vec3, double x, double y, double z) {
        vec3=new Vec3(x,y,z);
    }
    
    public void doTransform() {
        RenderUtils.glTranslate(VecUtils.add(transform, pivotPt));

        GL11.glRotated(rotation.xCoord, 1, 0, 0);
        GL11.glRotated(rotation.yCoord, 0, 1, 0);
        GL11.glRotated(rotation.zCoord, 0, 0, 1);
        
        GL11.glScaled(scale, scale, scale);
        
        RenderUtils.glTranslate(VecUtils.neg(pivotPt));
    }
    
}
