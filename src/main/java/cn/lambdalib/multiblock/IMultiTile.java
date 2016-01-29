/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.multiblock;

/**
 * Mark on any TileEntity that supports an BlockMulti. Provide the
 * InfoBlockMulti and handle the saving and loading (via loading from NBT) You
 * can visit TileMulti for impl reference.
 * 
 * @author WeathFolD
 */
public interface IMultiTile {

    InfoBlockMulti getBlockInfo();

    void setBlockInfo(InfoBlockMulti i);

}
