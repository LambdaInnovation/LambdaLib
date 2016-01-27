/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.ripple;

/**
 * Super class for all compiled function, used by code generator. You should not
 * directly implement this interface. Use NativeFunction instead.
 * 
 * @author acaly
 *
 */
public interface IFunction {

    Object call(Object[] args);

}
