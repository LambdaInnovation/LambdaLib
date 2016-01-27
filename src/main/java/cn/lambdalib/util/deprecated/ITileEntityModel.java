/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.deprecated;

import net.minecraft.tileentity.TileEntity;

/**
 * Interface for models applied on TileEntity.
 * @author WeAthFolD
 */
@Deprecated
public interface ITileEntityModel {

    public void render(TileEntity is, float f1, float f);
    
    public void renderPart(TileEntity te, String name , float f1, float f);
    
}
