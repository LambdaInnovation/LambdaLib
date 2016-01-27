/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.gui.event;


/**
 * Fired every frame for any widget if it is to be drawed. (i.e. <code>widget.transform.doesDraw=true</code>)
 */
public class FrameEvent implements GuiEvent {
    public final double mx, my;
    public final boolean hovering;
    
    public FrameEvent(double _mx, double _my, boolean hov) {
        mx = _mx;
        my = _my;
        hovering = hov;
    }
}
