/**
* Copyright (c) Lambda Innovation, 2013-2016
* This file is part of LambdaLib modding library.
* https://github.com/LambdaInnovation/LambdaLib
* Licensed under MIT, see project root for more information.
*/
package cn.lambdalib.cgui.gui.event;

import cn.lambdalib.cgui.gui.Widget;

/**
 * Fired on CGui when a new widget is added into it.
 */
public class AddWidgetEvent implements GuiEvent {
    
    public final Widget widget;
    
    public AddWidgetEvent(Widget w) {
        widget = w;
    }
    
}
