package cn.lambdalib.cgui.gui.event;

import cn.lambdalib.cgui.gui.Widget;

/**
 * Handler interface of <code>GuiEvent</code>, typically registered in a {@link cn.lambdalib.cgui.gui.event.GuiEventBus}.
 */
@FunctionalInterface
public interface IGuiEventHandler<T extends GuiEvent> {

	void handleEvent(Widget w, T event);

	/**
	 * Remove this event handler from the given bus, if it is in the bus.
	 */
	default void removeFrom(GuiEventBus bus) {
		bus.unlisten(this);
	}

	/**
	 * Remove this event handler from the given widget, if it is in the widget.
	 */
	default void removeFrom(Widget widget) {
		widget.unlisten(this);
	}
	
}
