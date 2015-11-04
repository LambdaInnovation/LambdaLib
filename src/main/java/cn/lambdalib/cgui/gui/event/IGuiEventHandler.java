package cn.lambdalib.cgui.gui.event;

import cn.lambdalib.cgui.gui.Widget;

public interface IGuiEventHandler<T extends GuiEvent> {

	void handleEvent(Widget w, T event);
	
}
