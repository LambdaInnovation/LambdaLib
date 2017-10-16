/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.util.ResourceLocation;

public class PartedModelHelper {
    
    public static Collection<CustomPartedModel> loadObjModelParts(ResourceLocation objLocation) {
        return loadObjModelParts(new WavefrontObject(objLocation));
    }
    
    public static Collection<CustomPartedModel> loadObjModelParts(WavefrontObject obj) {
        List<CustomPartedModel> ret = new ArrayList();
        for(GroupObject go : obj.groupObjects) {
            ret.add(new CustomPartedModel(obj, go.name));
        }
        return ret;
    }
    
    public static PartedModel loadObjModel(ResourceLocation location) {
        PartedModel ret = new PartedModel();
        for(CustomPartedModel part : loadObjModelParts(location)) {
            ret.addChild(part.name, part);
        }
        return ret;
    }
    
}
