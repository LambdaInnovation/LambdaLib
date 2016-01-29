/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.networkcall.s11n;

import net.minecraft.nbt.NBTBase;

public interface InstanceSerializer<T> {
    
    T readInstance(NBTBase nbt) throws Exception;
    NBTBase writeInstance(T obj) throws Exception;
    
}
