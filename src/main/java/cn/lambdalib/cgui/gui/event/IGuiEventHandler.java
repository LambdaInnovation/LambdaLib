package cn.lambdalib.cgui.gui.event;

import cn.lambdalib.cgui.gui.Widget;

/**
 * Handler interface of <code>GuiEvent</code>, typically registered in a {@link cn.lambdalib.cgui.gui.event.GuiEventBus}.
 */
@FunctionalInterface
public interface IGuiEventHandler<T extends GuiEvent> {

    void handleEvent(Widget w, T event);
    
}
