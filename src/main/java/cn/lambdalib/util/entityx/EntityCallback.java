/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.util.entityx;

import net.minecraft.entity.Entity;

/**
 * @author WeAthFolD
 */
public interface EntityCallback<T extends Entity> {

    void execute(T target);
    
}
