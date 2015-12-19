package cn.lambdalib.vis.model;

import java.io.IOException;

import cn.lambdalib.util.serialization.SerializeType;
import org.lwjgl.opengl.GL11;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import cn.lambdalib.util.client.RenderUtils;
import cn.lambdalib.util.generic.VecUtils;
import net.minecraft.util.Vec3;

@SerializeType
public class CompTransform {
	
	public Vec3 transform = Vec3.createVectorHelper(0, 0, 0);
	
	public Vec3 pivotPt = Vec3.createVectorHelper(0, 0, 0);
	
	public Vec3 rotation = Vec3.createVectorHelper(0, 0, 0);
	
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
		vec3.xCoord = x;
		vec3.yCoord = y;
		vec3.zCoord = z;
	}
	
	public void doTransform() {
		RenderUtils.glTranslate(VecUtils.add(transform, pivotPt));
		
		GL11.glRotated(rotation.zCoord, 0, 0, 1);
		GL11.glRotated(rotation.yCoord, 0, 1, 0);
		GL11.glRotated(rotation.xCoord, 1, 0, 0);
		
		GL11.glScaled(scale, scale, scale);
		
		RenderUtils.glTranslate(VecUtils.neg(pivotPt));
	}
	
}
