/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.networkcall.s11n;

import net.minecraft.nbt.NBTBase;

public interface DataSerializer<T> {

    /**
     * Note that obj can be null. When it's null, this function should create an instance.
     * In any cases, the result is returned.
     */
    T readData(NBTBase nbt, T obj) throws Exception;
    NBTBase writeData(T obj) throws Exception;
    
}
