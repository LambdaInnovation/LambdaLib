/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.animation.presets;

import java.util.HashMap;
import java.util.Map;

import cn.lambdalib.vis.animation.Animation;
import cn.lambdalib.vis.model.PartedModel;

/**
 * @author WeAthFolD
 */
public class PartedModelAnim extends Animation {
    
    public PartedModel target;
    
    private Map<String, CompTransformAnim> partAnims = new HashMap();
    
    CompTransformAnim directAnim;
    
    public PartedModelAnim(PartedModel model) {
        target = model;
    }
    
    public PartedModelAnim() {}
    
    public void init(CompTransformAnim anim) {
        directAnim = anim;
        anim.target = target.transform;
    }
    
    /**
     * Init animation of the given part name with the given animation.
     * The part must be of type ModelPart, otherwise an exception is thrown.
     */
    public void init(String partName, CompTransformAnim anim) {
        PartedModel part = target.getChild(partName);
        if(part != null) {
            partAnims.put(partName, anim);
            anim.target = part.transform;
        } else 
            throw new RuntimeException("Part " + partName + " invalid");
    }
    
    public CompTransformAnim animOf(String partName) {
        return partAnims.get(partName);
    }

    @Override
    public void perform(double timePoint) {
        for(CompTransformAnim anim : partAnims.values())
            anim.perform(timePoint);
        
        if(directAnim != null)
            directAnim.perform(timePoint);
    }

}
