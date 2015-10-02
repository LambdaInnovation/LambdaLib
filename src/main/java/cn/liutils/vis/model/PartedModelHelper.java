package cn.liutils.vis.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.WavefrontObject;

public class PartedModelHelper {
	
	public static Collection<CustomModelPart> loadObjModelParts(ResourceLocation objLocation) {
		return loadObjModelParts(new WavefrontObject(objLocation));
	}
	
	public static Collection<CustomModelPart> loadObjModelParts(WavefrontObject obj) {
		List<CustomModelPart> ret = new ArrayList();
		for(GroupObject go : obj.groupObjects) {
			ret.add(new CustomModelPart(obj, go.name));
		}
		return ret;
	}
	
	public static PartedModel loadObjModel(ResourceLocation location) {
		PartedModel ret = new PartedModel();
		for(CustomModelPart part : loadObjModelParts(location)) {
			ret.addChild(part.name, part);
		}
		return ret;
	}
	
}
