/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.gui.event;


/**
 * Fired on CGui and current focus when user presses left mouse button.
 */
public class LeftClickEvent implements GuiEvent {
    
    public final double x, y;
    
    public LeftClickEvent(double _x, double _y) {
        x = _x;
        y = _y;
    }

}
