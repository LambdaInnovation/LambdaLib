/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.vis.model;


/**
 * @author WeAthFolD
 */
public class CustomPartedModel extends PartedModel {

    public final IModelCustom model;
    public final String name;
    
    public CustomPartedModel(IModelCustom _model) {
        this(_model, null);
    }
    
    public CustomPartedModel(IModelCustom _model, String _part) {
        model = _model;
        name = _part;
    }
    
    @Override
    protected void handleDraw() {
        if(name == null) {
            model.renderAll();
        } else {
            model.renderPart(name);
        }
    }
    

}
