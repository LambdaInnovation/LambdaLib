/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.crafting;

/**
 * In a recipe file, use "NAME#DATA*AMOUNT" to specify an element.
 * 
 * @author EAirPeter
 */
public class ParsedRecipeElement {

    public String name = null;
    public int data = 0;
    public int amount = 1;

    public boolean dataParsed = false;

    @Override
    public String toString() {
        return "(" + name + "," + data + "," + amount + ")";
    }

}
