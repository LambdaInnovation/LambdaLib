/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.IModelCustom;

/**
 * Simple adaptor from IModelCustom to ITileEntityModel.
 * @author WeAthFolD
 */
public class TileEntityModelCustom implements ITileEntityModel {
    
    IModelCustom theModel;

    public TileEntityModelCustom(IModelCustom model) {
        theModel = model;
    }

    @Override
    public void render(TileEntity is, float f1, float f) {
        theModel.renderAll();
    }

    @Override
    public void renderPart(TileEntity te, String name, float f1, float f) {
        theModel.renderPart(name);
    }

}
