/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.model;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

/**
 * @author WeAthFolD
 */
public class PartedModel implements IModel {
    
    public final CompTransform transform = new CompTransform();
    
    public boolean doesDraw = true;
    
    Map<String, PartedModel> childs = new HashMap();
    
    public PartedModel() {}
    
    public void addChild(String name, PartedModel child) {
        childs.put(name, child);
    }
    
    public <T extends PartedModel> T getChild(String name) {
        return (T) childs.get(name);
    }
    
    public void draw() {
        if(!doesDraw)
            return;
        
        GL11.glPushMatrix();
        
        transform.doTransform();
        for(IModel model : childs.values())
            model.draw();
        
        handleDraw();
        
        GL11.glPopMatrix();
    }
    
    protected void handleDraw() {}
    
}
