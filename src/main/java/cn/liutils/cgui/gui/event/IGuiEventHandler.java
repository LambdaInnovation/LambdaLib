package cn.liutils.cgui.gui.event;

import cn.liutils.cgui.gui.Widget;

public interface IGuiEventHandler<T extends GuiEvent> {

	void handleEvent(Widget w, T event);
	
}
